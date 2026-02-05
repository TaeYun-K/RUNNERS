package com.runners.app.notification.dto.response;

import java.util.List;

/**
 * 알림 목록 응답 DTO (커서 기반 페이지네이션)
 */
public record NotificationCursorListResponse(
        List<NotificationResponse> notifications,
        Boolean hasNext,
        String nextCursor  // "createdAt,id" 형식 또는 null
) {}
