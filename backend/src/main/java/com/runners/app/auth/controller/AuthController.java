package com.runners.app.auth.controller;

import com.runners.app.auth.dto.GoogleLoginRequest;
import com.runners.app.auth.dto.GoogleLoginResponse;
import com.runners.app.auth.dto.RefreshTokenRequest;
import com.runners.app.auth.dto.TokenRefreshResponse;
import com.runners.app.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @Operation(summary = "Google 로그인", description = "Android에서 받은 idToken을 검증하고 사용자 정보를 반환")
    @PostMapping("/google")
    public GoogleLoginResponse googleLogin(@RequestBody GoogleLoginRequest req) {
        if (req.idToken() == null || req.idToken().isBlank()) {
            throw new IllegalArgumentException("idToken is required");
        }
        return authService.googleLogin(req.idToken());
    }

    @Operation(summary = "Access Token 재발급", description = "Refresh Token을 검증한 뒤 Access Token을 재발급합니다.")
    @PostMapping("/refresh")
    public TokenRefreshResponse refresh(@RequestBody RefreshTokenRequest req) {
        if (req.refreshToken() == null || req.refreshToken().isBlank()) {
            throw new IllegalArgumentException("refreshToken is required");
        }
        return authService.refreshAccessToken(req.refreshToken());
    }
}
