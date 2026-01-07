package com.runners.app.community.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommunityPostRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String content
) {}

