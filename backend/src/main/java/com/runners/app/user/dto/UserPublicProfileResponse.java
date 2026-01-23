package com.runners.app.user.dto;

public record UserPublicProfileResponse(
        Long userId,
        String displayName,
        String nickname,
        String intro,
        String picture,
        Double totalDistanceKm,
        Long totalDurationMinutes,
        Integer runCount
) {}

