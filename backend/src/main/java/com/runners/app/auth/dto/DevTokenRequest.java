package com.runners.app.auth.dto;

import com.runners.app.global.validation.ValidationMessageKey;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DevTokenRequest(
        @NotBlank(message = ValidationMessageKey.DEV_EMAIL_REQUIRED)
        @Email(message = ValidationMessageKey.DEV_EMAIL_INVALID_FORMAT)
        @Size(max = 320, message = ValidationMessageKey.DEV_EMAIL_TOO_LONG)
        String email,
        @Size(max = 100, message = ValidationMessageKey.DEV_NAME_TOO_LONG) String name,
        @Size(max = 50, message = ValidationMessageKey.DEV_ROLE_TOO_LONG) String role
) {}
