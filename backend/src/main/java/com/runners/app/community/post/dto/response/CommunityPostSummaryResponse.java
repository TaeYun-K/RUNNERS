package com.runners.app.community.post.dto.response;

import java.time.LocalDateTime;

public record CommunityPostSummaryResponse(
        Long postId,
        Long authorId,
        String authorName,
        Double authorTotalDistanceKm,
        String title,
        String contentPreview,
        String thumbnailUrl,
        int viewCount,
        int recommendCount,
        int commentCount,
        LocalDateTime createdAt
) {}
