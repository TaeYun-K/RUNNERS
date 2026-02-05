package com.runners.app.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * FCM 토큰 등록 요청 DTO
 */
public record RegisterDeviceTokenRequest(
        @NotBlank(message = "Token is required")
        @Size(max = 500, message = "Token is too long")
        String token,

        @Size(max = 100, message = "Device ID is too long")
        String deviceId,

        @Size(max = 20, message = "Platform is too long")
        String platform
) {}
