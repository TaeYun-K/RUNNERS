package com.runners.app.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileImageRequest(
        @NotBlank @Size(max = 1024) String key
) {}

