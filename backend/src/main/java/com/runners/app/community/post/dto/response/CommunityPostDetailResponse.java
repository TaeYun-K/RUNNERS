package com.runners.app.community.post.dto.response;

import com.runners.app.community.post.entity.CommunityPostBoardType;
import java.time.LocalDateTime;
import java.util.List;

public record CommunityPostDetailResponse(
        Long postId,
        Long authorId,
        String authorName,
        String authorPicture,
        Double authorTotalDistanceKm,
        CommunityPostBoardType boardType,
        String title,
        String content,
        List<String> imageKeys,
        List<String> imageUrls,
        int viewCount,
        int recommendCount,
        int commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
