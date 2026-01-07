package com.runners.app.user.dto;

public record UserMeResponse(
        Long userId,
        String email,
        String name,
        String picture,
        String role
) {}

