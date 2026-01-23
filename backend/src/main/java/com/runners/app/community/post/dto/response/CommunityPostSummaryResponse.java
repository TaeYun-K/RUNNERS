package com.runners.app.community.post.dto.response;

import com.runners.app.community.post.entity.CommunityPostBoardType;
import java.time.LocalDateTime;

public record CommunityPostSummaryResponse(
        Long postId,
        Long authorId,
        String authorName,
        String authorPicture,
        Double authorTotalDistanceKm,
        CommunityPostBoardType boardType,
        String title,
        String contentPreview,
        String thumbnailUrl,
        int viewCount,
        int recommendCount,
        int commentCount,
        LocalDateTime createdAt
) {}
