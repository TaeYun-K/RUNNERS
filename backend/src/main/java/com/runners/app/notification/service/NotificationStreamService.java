package com.runners.app.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runners.app.community.comment.event.CommentCreatedEvent;
import com.runners.app.notification.entity.NotificationOutbox;
import com.runners.app.notification.entity.OutboxStatus;
import com.runners.app.notification.repository.NotificationOutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.Map;

/**
 * Redis Stream 적재 전용 서비스
 * 하이브리드 방식: Redis Stream + DB Outbox
 */
@Slf4j
@Service
public class NotificationStreamService {

    private static final String STREAM_KEY = "notification:events:comment-created";
    private static final String CONSUMER_GROUP = "notification-workers";

    private final StringRedisTemplate notificationRedisTemplate;  // DB 2번 사용
    private final NotificationOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public NotificationStreamService(
            @Qualifier("notificationStringRedisTemplate") StringRedisTemplate notificationRedisTemplate,
            NotificationOutboxRepository outboxRepository,
            ObjectMapper objectMapper
    ) {
        this.notificationRedisTemplate = notificationRedisTemplate;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Consumer Group 초기화 (백엔드 애플리케이션 시작 시)
     * 
     * 주의: Redis Stream이 존재하지 않으면 Consumer Group을 생성할 수 없습니다.
     * 첫 번째 메시지가 발행될 때 Stream이 자동으로 생성되므로,
     * Stream이 없는 경우 예외는 무시됩니다.
     */
    @PostConstruct
    public void initializeConsumerGroup() {
        try {
            // Consumer Group 생성 (이미 존재하면 무시, DB 2번 사용)
            // 처음부터 읽기 위해 ReadOffset.from("0") 사용
            notificationRedisTemplate.opsForStream().createGroup(
                    STREAM_KEY,
                    ReadOffset.from("0"),
                    CONSUMER_GROUP
            );
            log.info("Notification consumer group initialized: {}", CONSUMER_GROUP);
        } catch (Exception e) {
            // Consumer Group이 이미 존재하거나 Stream이 없는 경우 무시
            // Stream이 없으면 첫 번째 메시지 발행 시 자동으로 생성됨
            log.debug("Consumer group already exists or stream not found: {}", e.getMessage());
        }
    }

    /**
     * Redis Stream에 이벤트 발행 시도
     * 실패 시 DB Outbox에 저장하여 유실 방지
     */
    public void publishEvent(CommentCreatedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            Map<String, String> fields = Map.of(
                    "eventType", "COMMENT_CREATED",
                    "payload", payload
            );

            // Redis Stream에 이벤트 발행 시도 (DB 2번 사용)
            notificationRedisTemplate.opsForStream().add(STREAM_KEY, fields);
            log.debug("Published event to Redis Stream: commentId={}", event.commentId());

        } catch (Exception e) {
            // Redis Stream 발행 실패 시 DB Outbox에 저장
            log.warn("Failed to publish to Redis Stream, saving to Outbox: commentId={}", 
                    event.commentId(), e);
            saveToOutbox(event);
        }
    }

    /**
     * Outbox에 저장 (별도 트랜잭션)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveToOutbox(CommentCreatedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            NotificationOutbox outbox = NotificationOutbox.builder()
                    .eventType("COMMENT_CREATED")
                    .payload(payload)
                    .status(OutboxStatus.PENDING)
                    .build();

            outboxRepository.save(outbox);
            log.info("Saved event to Outbox: commentId={}, outboxId={}", 
                    event.commentId(), outbox.getId());

        } catch (Exception e) {
            log.error("Failed to save to Outbox: commentId={}", event.commentId(), e);
            throw new RuntimeException("Failed to save event to Outbox", e);
        }
    }
}
