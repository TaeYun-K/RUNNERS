package com.runners.app.user.dto;

public record UserMeResponse(
        Long userId,
        String email,
        String name,
        String nickname,
        String picture,
        String role,
        Double totalDistanceKm
) {}
