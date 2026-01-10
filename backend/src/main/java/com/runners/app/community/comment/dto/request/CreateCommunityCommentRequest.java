package com.runners.app.community.comment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCommunityCommentRequest(
        @NotBlank String content,
        Long parentId
) {}

