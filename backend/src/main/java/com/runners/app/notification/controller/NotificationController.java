package com.runners.app.notification.controller;

import com.runners.app.global.util.SecurityUtils;
import com.runners.app.notification.dto.response.NotificationCursorListResponse;
import com.runners.app.notification.dto.response.UnreadNotificationCountResponse;
import com.runners.app.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "알림 목록 조회", description = "커서 기반 페이지네이션으로 알림 목록을 조회합니다.")
    @GetMapping
    public NotificationCursorListResponse listNotifications(
            Authentication authentication,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = SecurityUtils.extractUserId(authentication);
        return notificationService.listNotifications(userId, cursor, size);
    }

    @Operation(summary = "안읽음 알림 개수 조회", description = "사용자의 안읽음 알림 개수를 조회합니다.")
    @GetMapping("/unread-count")
    public UnreadNotificationCountResponse getUnreadCount(Authentication authentication) {
        Long userId = SecurityUtils.extractUserId(authentication);
        return notificationService.getUnreadCount(userId);
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리합니다.")
    @PutMapping("/{notificationId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(
            Authentication authentication,
            @PathVariable Long notificationId
    ) {
        Long userId = SecurityUtils.extractUserId(authentication);
        notificationService.markAsRead(userId, notificationId);
    }

    @Operation(summary = "모든 알림 읽음 처리", description = "사용자의 모든 알림을 읽음 처리합니다.")
    @PutMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllAsRead(Authentication authentication) {
        Long userId = SecurityUtils.extractUserId(authentication);
        notificationService.markAllAsRead(userId);
    }
}
