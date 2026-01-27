package com.runners.app.user.dto;

import com.runners.app.global.validation.ValidationMessageKey;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileImageRequest(
        @NotBlank(message = ValidationMessageKey.PROFILE_IMAGE_KEY_REQUIRED)
        @Size(max = 1024, message = ValidationMessageKey.PROFILE_IMAGE_KEY_INVALID)
        String key
) {}
