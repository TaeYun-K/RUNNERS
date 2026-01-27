package com.runners.app.user.dto;

import com.runners.app.global.validation.ValidationMessageKey;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 2, message = ValidationMessageKey.NICKNAME_TOO_SHORT)
        @Size(max = 20, message = ValidationMessageKey.NICKNAME_TOO_LONG)
        @Pattern(
                regexp = "^[A-Za-z0-9가-힣_ ]+$",
                message = ValidationMessageKey.NICKNAME_INVALID_FORMAT
        )
        String nickname,

        @Size(max = 30, message = ValidationMessageKey.INTRO_TOO_LONG)
        String intro
) {}
