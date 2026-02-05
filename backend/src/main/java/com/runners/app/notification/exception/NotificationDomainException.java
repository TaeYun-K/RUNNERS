package com.runners.app.notification.exception;

import com.runners.app.global.exception.DomainException;
import com.runners.app.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class NotificationDomainException extends DomainException {
    public NotificationDomainException(HttpStatus status, ErrorCode errorCode, String message) {
        super(status, errorCode, message);
    }

    public static NotificationDomainException notificationNotFound() {
        return new NotificationDomainException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Notification not found");
    }

    public static NotificationDomainException noPermission() {
        return new NotificationDomainException(HttpStatus.FORBIDDEN, ErrorCode.NO_PERMISSION, "No permission to access this notification");
    }

    public static NotificationDomainException deviceTokenRegistrationFailed(String message) {
        return new NotificationDomainException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "Device token registration failed: " + message);
    }

    public static NotificationDomainException fcmSendFailed(String message) {
        return new NotificationDomainException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, "FCM send failed: " + message);
    }
}
