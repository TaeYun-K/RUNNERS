package com.runners.app.auth.controller;

import com.runners.app.auth.dto.GoogleLoginRequest;
import com.runners.app.auth.dto.GoogleLoginResponse;
import com.runners.app.auth.dto.TokenRefreshResponse;
import com.runners.app.auth.cookie.RefreshTokenCookie;
import com.runners.app.auth.exception.AuthDomainException;
import com.runners.app.auth.service.AuthService;
import com.runners.app.auth.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

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
            @CookieValue(name = RefreshTokenCookie.COOKIE_NAME, required = false) String refreshToken,
            HttpServletRequest request
    ) {
        if (request.getContentLengthLong() > 0) {
            throw AuthDomainException.requestBodyNotAllowed();
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            throw AuthDomainException.refreshTokenMissing();
        }
        return authService.refreshAccessToken(refreshToken);
    }

    @Operation(summary = "로그아웃", description = "Refresh Token을 무효화하고 쿠키를 제거합니다.")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @CookieValue(name = RefreshTokenCookie.COOKIE_NAME, required = false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (request.getContentLengthLong() > 0) {
            throw AuthDomainException.requestBodyNotAllowed();
        }
        authService.logout(refreshToken);
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                RefreshTokenCookie.clear(request).toString()
        );
    }
}
