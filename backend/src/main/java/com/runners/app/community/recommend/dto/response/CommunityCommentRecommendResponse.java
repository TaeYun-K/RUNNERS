package com.runners.app.community.recommend.dto.response;

public record CommunityCommentRecommendResponse(
        Long postId,
        Long commentId,
        boolean recommended,
        int recommendCount
) {}

