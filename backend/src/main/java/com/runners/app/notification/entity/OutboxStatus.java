package com.runners.app.notification.entity;

/**
 * NotificationOutbox의 처리 상태
 */
public enum OutboxStatus {
    PENDING,    // Redis Stream 발행 대기 중
    PUBLISHED   // Redis Stream 발행 완료
}
