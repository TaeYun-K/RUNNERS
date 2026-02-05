package com.runners.app.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runners.app.community.comment.event.CommentCreatedEvent;
import com.runners.app.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

/**
 * Redis Stream 메시지 리스너
 * 전용 리스너 컨테이너에서 호출됨
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationStreamMessageListener
        implements StreamListener<String, MapRecord<String, String, String>> {

    private static final String STREAM_KEY = "notification:events:comment-created";
    private static final String CONSUMER_GROUP = "notification-workers";

    private final StringRedisTemplate notificationRedisTemplate;  // DB 2번 사용
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(MapRecord<String, String, String> record) {
        String recordId = record.getId().getValue();

        try {
            // 이벤트 역직렬화
            String payload = record.getValue().get("payload");
            CommentCreatedEvent event = objectMapper.readValue(payload, CommentCreatedEvent.class);

            // 알림 처리
            notificationService.processEvent(event);

            // 처리 완료 확인 (ACK, DB 2번 사용)
            notificationRedisTemplate.opsForStream().acknowledge(STREAM_KEY, CONSUMER_GROUP, recordId);
            log.debug("Processed and acknowledged stream record: {}", recordId);

        } catch (Exception e) {
            log.error("Failed to process stream record: {}", recordId, e);
            // 실패 시 ACK하지 않으면 자동으로 PENDING 리스트에 추가됨
            // 재시도는 NotificationPendingReprocessor에서 처리
        }
    }
}
