package com.runners.app.community.upload.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record PresignCommunityImageUploadResponse(
        List<PresignedCommunityUploadItem> items,
        LocalDateTime expiresAt
) {}

