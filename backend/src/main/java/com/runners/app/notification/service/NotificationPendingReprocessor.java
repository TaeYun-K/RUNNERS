package com.runners.app.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runners.app.community.comment.event.CommentCreatedEvent;
import com.runners.app.community.recommend.event.CommentRecommendedEvent;
import com.runners.app.community.recommend.event.PostRecommendedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.ByteRecord;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.time.Duration;
import java.util.List;

/**
 * PENDING 리스트에서 idle time 기반으로 재처리
 * 30초마다 실행되어 처리 실패한 메시지를 재시도
 */
@Slf4j
@Component
public class NotificationPendingReprocessor {

    private static final String STREAM_KEY = "notification:events:comment-created";
    private static final String CONSUMER_GROUP = "notification-workers";
    private static final String CONSUMER_NAME = "worker-" + getHostname() + "-" + ProcessHandle.current().pid();
    private static final int BATCH_SIZE = 10;
    private static final Duration MIN_IDLE_TIME = Duration.ofMinutes(1);  // 1분 이상 idle인 것만 재처리

    private final StringRedisTemplate notificationRedisTemplate;  // DB 2번 사용
    private final RedisConnectionFactory notificationRedisConnectionFactory;  // DB 2번 사용
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public NotificationPendingReprocessor(
            @Qualifier("notificationStringRedisTemplate") StringRedisTemplate notificationRedisTemplate,
            @Qualifier("notificationRedisConnectionFactory") RedisConnectionFactory notificationRedisConnectionFactory,
            NotificationService notificationService,
            ObjectMapper objectMapper
    ) {
        this.notificationRedisTemplate = notificationRedisTemplate;
        this.notificationRedisConnectionFactory = notificationRedisConnectionFactory;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    /**
     * PENDING 리스트에서 idle time 기반으로 재처리
     */
    @Scheduled(fixedDelay = 30000)  // 30초마다 실행
    public void processPendingMessages() {
        try (RedisConnection connection = notificationRedisConnectionFactory.getConnection()) {
            // PENDING 리스트 조회 (DB 2번 사용)
            RedisStreamCommands streamCommands = connection.streamCommands();
            PendingMessages pendingMessages = streamCommands.xPending(
                    STREAM_KEY.getBytes(),
                    CONSUMER_GROUP,
                    RedisStreamCommands.XPendingOptions.unbounded((long) BATCH_SIZE)
            );

            if (pendingMessages == null || pendingMessages.isEmpty()) {
                return;
            }

            long minIdleTimeMillis = MIN_IDLE_TIME.toMillis();

            for (PendingMessage pending : pendingMessages) {
                // idle time 확인 (밀리초 단위)
                // getElapsedTimeSinceLastDelivery()는 Duration을 반환하므로 밀리초로 변환
                long idleTime = pending.getElapsedTimeSinceLastDelivery().toMillis();

                if (idleTime >= minIdleTimeMillis) {
                    // idle time이 충분히 긴 메시지만 재처리
                    claimAndProcess(pending, connection);
                }
            }

        } catch (Exception e) {
            log.error("Error processing pending messages", e);
        }
    }

    private void claimAndProcess(PendingMessage pending, RedisConnection connection) {
        try {
            String recordId = pending.getIdAsString();
            RecordId recordIdObj = RecordId.of(recordId);

            // 메시지 재처리 권한 획득 (DB 2번 사용)
            RedisStreamCommands streamCommands = connection.streamCommands();
            List<ByteRecord> claimed = streamCommands.xClaim(
                    STREAM_KEY.getBytes(),
                    CONSUMER_GROUP,
                    CONSUMER_NAME,
                    MIN_IDLE_TIME,
                    recordIdObj
            );

            if (claimed != null && !claimed.isEmpty()) {
                MapRecord<byte[], byte[], byte[]> record = claimed.get(0);

                // 이벤트 역직렬화 및 처리
                byte[] eventTypeBytes = record.getValue().get("eventType".getBytes());
                byte[] payloadBytes = record.getValue().get("payload".getBytes());
                if (payloadBytes != null) {
                    String eventType = eventTypeBytes == null ? null : new String(eventTypeBytes);
                    String payload = new String(payloadBytes);
                    processEvent(eventType, payload);

                    // 처리 완료 확인 (ACK, DB 2번 사용)
                    streamCommands.xAck(STREAM_KEY.getBytes(), CONSUMER_GROUP, recordIdObj);
                    log.info("Successfully reprocessed pending message: {}", recordId);
                }
            }

        } catch (Exception e) {
            log.error("Failed to claim and process pending message: {}", pending.getIdAsString(), e);
        }
    }

    private void processEvent(String eventType, String payload) throws Exception {
        if (eventType == null || payload == null) {
            log.warn("Invalid pending stream record: eventType={}", eventType);
            return;
        }

        switch (eventType) {
            case "COMMENT_CREATED" -> {
                CommentCreatedEvent event = objectMapper.readValue(payload, CommentCreatedEvent.class);
                notificationService.processEvent(event);
            }
            case "COMMENT_RECOMMENDED" -> {
                CommentRecommendedEvent event = objectMapper.readValue(
                        payload,
                        CommentRecommendedEvent.class
                );
                notificationService.processCommentRecommendEvent(event);
            }
            case "POST_RECOMMENDED" -> {
                PostRecommendedEvent event = objectMapper.readValue(payload, PostRecommendedEvent.class);
                notificationService.processPostRecommendEvent(event);
            }
            default -> log.warn("Unknown pending stream event type: {}", eventType);
        }
    }

    private static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
