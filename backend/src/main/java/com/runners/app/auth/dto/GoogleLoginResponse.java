package com.runners.app.auth.dto;

public record GoogleLoginResponse(
        Long userId,
        String email,
        String name,
        String picture,
        String accessToken,
        String refreshToken,
        boolean isNewUser
) {}
