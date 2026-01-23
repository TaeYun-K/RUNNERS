package com.runners.app.community.post.dto.response;

import com.runners.app.community.post.entity.CommunityPostBoardType;
import java.time.LocalDateTime;
import java.util.List;

public record CommunityPostResponse(
        Long postId,
        Long authorId,
        String authorName,
        String authorPicture,
        CommunityPostBoardType boardType,
        String title,
        String content,
        int viewCount,
        int recommendCount,
        int commentCount,
        LocalDateTime createdAt,
        List<String> imageUrls
) {}
