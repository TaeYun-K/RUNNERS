package com.runners.app.notification.controller;

import com.runners.app.global.util.SecurityUtils;
import com.runners.app.notification.dto.request.RegisterDeviceTokenRequest;
import com.runners.app.notification.service.DeviceTokenService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/device-tokens")
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    public DeviceTokenController(DeviceTokenService deviceTokenService) {
        this.deviceTokenService = deviceTokenService;
    }

    @Operation(summary = "FCM 토큰 등록/업데이트", description = "FCM 푸시 알림을 받기 위한 디바이스 토큰을 등록하거나 업데이트합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void registerToken(
            Authentication authentication,
            @Valid @RequestBody RegisterDeviceTokenRequest request
    ) {
        Long userId = SecurityUtils.extractUserId(authentication);
        deviceTokenService.registerToken(userId, request.token(), request.deviceId(), request.platform());
    }

    @Operation(summary = "FCM 토큰 삭제", description = "로그아웃 시 FCM 토큰을 삭제합니다.")
    @DeleteMapping("/{token}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeToken(
            Authentication authentication,
            @PathVariable String token
    ) {
        Long userId = SecurityUtils.extractUserId(authentication);
        deviceTokenService.removeToken(userId, token);
    }
}
