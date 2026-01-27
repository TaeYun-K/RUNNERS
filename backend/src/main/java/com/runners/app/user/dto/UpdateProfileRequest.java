package com.runners.app.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 2, max = 20)
        @Pattern(
                regexp = "^[A-Za-z0-9가-힣_ ]+$",
                message = "Nickname can contain Korean/English letters, numbers, and underscore"
        )
        String nickname,

        @Size(max = 30)
        String intro
) {}

