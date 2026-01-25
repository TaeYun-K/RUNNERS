package com.runners.app.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record GoogleLoginResponse(
        Long userId,
        String email,
        String name,
        String nickname,
        String picture,
        String accessToken,
        @JsonIgnore String refreshToken,
        boolean isNewUser
) {}
