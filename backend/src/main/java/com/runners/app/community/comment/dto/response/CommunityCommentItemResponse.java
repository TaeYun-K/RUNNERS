package com.runners.app.community.comment.dto.response;

import java.time.LocalDateTime;

public record CommunityCommentItemResponse(
        Long commentId,
        Long authorId,
        String authorName,
        String authorPicture,
        Double authorTotalDistanceKm,
        Long parentId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

