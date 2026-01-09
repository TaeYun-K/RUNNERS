package com.runners.app.auth.dto;

public record GoogleLoginResponse(
        Long userId,
        String email,
        String name,
        String nickname,
        String picture,
        String accessToken,
        String refreshToken,
        boolean isNewUser
) {}
