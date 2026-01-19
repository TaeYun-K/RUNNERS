package com.runners.app.community.comment.dto.response;

public record CommunityCommentMutationResponse(
    CommunityCommentResponse comment,
    int commentCount
) {}

