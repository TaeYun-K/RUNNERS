package com.runners.app.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runners.app.community.comment.event.CommentCreatedEvent;
import com.runners.app.community.recommend.event.CommentRecommendedEvent;
import com.runners.app.community.recommend.event.PostRecommendedEvent;
import com.runners.app.notification.entity.NotificationOutbox;
import com.runners.app.notification.entity.OutboxStatus;
import com.runners.app.notification.repository.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Outbox에서 Redis Stream으로 재발행하는 서비스
 * Redis Stream 발행 실패 시 Outbox에 저장된 이벤트를 재발행
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationOutboxRepublishService {

    private final NotificationOutboxRepository outboxRepository;
    private final NotificationStreamService streamService;
    private final ObjectMapper objectMapper;

    /**
     * PENDING 상태의 Outbox를 Redis Stream에 재발행
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void republishPendingEvents() {
        // PENDING 상태의 Outbox 조회 (최대 10개)
        List<NotificationOutbox> pendingOutboxes = outboxRepository
                .findPendingForRepublish(OutboxStatus.PENDING, PageRequest.of(0, 10));

        if (pendingOutboxes.isEmpty()) {
            return;
        }

        log.debug("Found {} pending outbox events to republish", pendingOutboxes.size());

        for (NotificationOutbox outbox : pendingOutboxes) {
            try {
                republishByType(outbox);

                // 성공 시 상태를 PUBLISHED로 변경
                outbox.markAsPublished();
                outboxRepository.save(outbox);
                log.info("Successfully republished outbox event: outboxId={}, eventType={}",
                        outbox.getId(), outbox.getEventType());

            } catch (Exception e) {
                log.error("Failed to republish outbox: outboxId={}", outbox.getId(), e);
                // 실패 시 다음 주기에 재시도
            }
        }
    }

    private void republishByType(NotificationOutbox outbox) throws Exception {
        String eventType = outbox.getEventType();
        String payload = outbox.getPayload();

        if (eventType == null || payload == null) {
            throw new IllegalStateException("Invalid outbox event data");
        }

        switch (eventType) {
            case "COMMENT_CREATED" -> {
                CommentCreatedEvent event = objectMapper.readValue(payload, CommentCreatedEvent.class);
                streamService.publishEvent(event);
            }
            case "COMMENT_RECOMMENDED" -> {
                CommentRecommendedEvent event = objectMapper.readValue(
                        payload,
                        CommentRecommendedEvent.class
                );
                streamService.publishEvent(event);
            }
            case "POST_RECOMMENDED" -> {
                PostRecommendedEvent event = objectMapper.readValue(payload, PostRecommendedEvent.class);
                streamService.publishEvent(event);
            }
            default -> throw new IllegalArgumentException("Unsupported outbox event type: " + eventType);
        }
    }
}
