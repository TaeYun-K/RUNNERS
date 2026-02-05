package com.runners.app.notification.dto.response;

import com.runners.app.notification.entity.NotificationType;

import java.time.LocalDateTime;

/**
 * 알림 응답 DTO
 */
public record NotificationResponse(
        Long id,
        NotificationType type,
        Long relatedPostId,
        Long relatedCommentId,
        Long actorId,
        String actorName,
        String actorPicture,
        boolean isRead,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {}
