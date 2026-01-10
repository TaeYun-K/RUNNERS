package com.runners.app.community.comment.dto.response;

import java.util.List;

public record CommunityCommentCursorListResponse(
        List<CommunityCommentItemResponse> comments,
        String nextCursor
) {}

