package com.runners.app.community.recommend.dto.response;

public record CommunityPostRecommendResponse(
        Long postId,
        boolean recommended,
        int recommendCount
) {}

