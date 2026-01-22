package com.runners.app.community.upload.dto.response;

public record PresignedCommunityUploadItem(
        String key,
        String uploadUrl,
        String fileUrl,
        String contentType
) {}

