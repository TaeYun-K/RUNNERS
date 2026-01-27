package com.runners.app.community.comment.dto.request;

import com.runners.app.global.validation.ValidationMessageKey;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommunityCommentRequest(
        @NotBlank(message = ValidationMessageKey.CONTENT_REQUIRED)
        @Size(max = 16000, message = ValidationMessageKey.CONTENT_TOO_LONG)
        String content,
        Long parentId
) {}
