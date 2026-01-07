package com.runners.app.community.post.dto.response;

import java.time.LocalDateTime;

public record CommunityPostDetailResponse(
        Long postId,
        Long authorId,
        String authorName,
        String authorPicture,
        String title,
        String content,
        int viewCount,
        int recommendCount,
        int commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

