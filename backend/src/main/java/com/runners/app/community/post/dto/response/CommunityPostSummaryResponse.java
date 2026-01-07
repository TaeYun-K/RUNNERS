package com.runners.app.community.post.dto.response;

import java.time.LocalDateTime;

public record CommunityPostSummaryResponse(
        Long postId,
        Long authorId,
        String authorName,
        String title,
        String contentPreview,
        int viewCount,
        int recommendCount,
        int commentCount,
        LocalDateTime createdAt
) {}
