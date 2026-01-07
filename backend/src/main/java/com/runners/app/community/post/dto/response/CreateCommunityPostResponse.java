package com.runners.app.community.post.dto.response;

import java.time.LocalDateTime;

public record CreateCommunityPostResponse(
        Long postId,
        Long authorId,
        String title,
        String content,
        int viewCount,
        int recommendCount,
        int commentCount,
        LocalDateTime createdAt
) {}

