package com.runners.app.user.dto;

public record UserMeResponse(
        Long userId,
        String email,
        String name,
        String nickname,
        String intro,
        String picture,
        String role,
        Double totalDistanceKm
) {}
