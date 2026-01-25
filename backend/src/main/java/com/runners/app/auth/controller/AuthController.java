package com.runners.app.auth.controller;

import com.runners.app.auth.dto.GoogleLoginRequest;
import com.runners.app.auth.dto.GoogleLoginResponse;
import com.runners.app.auth.dto.TokenRefreshResponse;
import com.runners.app.auth.cookie.RefreshTokenCookie;
import com.runners.app.auth.service.AuthService;
import com.runners.app.auth.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }
    @Operation(summary = "Google 로그인", description = "Android에서 받은 idToken을 검증하고 사용자 정보를 반환")
    @PostMapping("/google")
    public GoogleLoginResponse googleLogin(
            @RequestBody GoogleLoginRequest req,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (req.idToken() == null || req.idToken().isBlank()) {
            throw new IllegalArgumentException("idToken is required");
        }
        GoogleLoginResponse result = authService.googleLogin(req.idToken());
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                RefreshTokenCookie.create(request, result.refreshToken(), jwtService.refreshTokenTtl()).toString()
        );
        return result;
    }

    @Operation(summary = "Access Token 재발급", description = "Refresh Token을 검증한 뒤 Access Token을 재발급합니다.")
    @PostMapping("/refresh")
    public TokenRefreshResponse refresh(
            @CookieValue(name = RefreshTokenCookie.COOKIE_NAME, required = false) String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing refresh token");
        }
        return authService.refreshAccessToken(refreshToken);
    }
}
