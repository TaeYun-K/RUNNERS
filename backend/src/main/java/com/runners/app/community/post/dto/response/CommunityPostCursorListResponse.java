package com.runners.app.community.post.dto.response;

import java.util.List;

public record CommunityPostCursorListResponse(
        List<CommunityPostSummaryResponse> posts,
        String nextCursor
) {}

