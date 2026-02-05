package com.runners.app.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Outbox에서 Redis Stream으로 재발행하는 워커
 * 10초마다 실행되어 PENDING 상태의 Outbox를 Redis Stream에 재발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationOutboxRepublishWorker {

    private final NotificationOutboxRepublishService republishService;

    /**
     * Outbox에서 Redis Stream으로 재발행
     */
    @Scheduled(fixedDelay = 10000)  // 10초마다 실행
    public void republishPendingEvents() {
        try {
            republishService.republishPendingEvents();
        } catch (Exception e) {
            log.error("Error in outbox republish worker", e);
        }
    }
}
