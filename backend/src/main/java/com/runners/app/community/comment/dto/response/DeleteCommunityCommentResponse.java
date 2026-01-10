package com.runners.app.community.comment.dto.response;

import java.time.LocalDateTime;

public record DeleteCommunityCommentResponse(
        Long commentId,
        Long postId,
        int commentCount,
        LocalDateTime deletedAt
) {}

