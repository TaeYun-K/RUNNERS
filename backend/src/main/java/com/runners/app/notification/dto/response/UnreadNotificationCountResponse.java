package com.runners.app.notification.dto.response;

/**
 * 안읽음 알림 개수 응답 DTO
 */
public record UnreadNotificationCountResponse(
        long unreadCount
) {}
