package com.runners.app.community.comment.dto.response;

import java.time.LocalDateTime;

public record CommunityCommentResponse(
        Long commentId,
        Long postId,
        Long authorId,
        Long parentId,
        String content,
        int commentCount,
        LocalDateTime createdAt
) {}

