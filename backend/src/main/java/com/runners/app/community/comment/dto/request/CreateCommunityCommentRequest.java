package com.runners.app.community.comment.dto.request;

import com.runners.app.global.validation.ValidationMessageKey;
import jakarta.validation.constraints.NotBlank;

public record CreateCommunityCommentRequest(
        @NotBlank(message = ValidationMessageKey.CONTENT_REQUIRED) String content,
        Long parentId
) {}
