package com.runners.app.community.upload.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PresignCommunityImageUploadFileRequest(
        @Size(max = 255) String fileName,
        @NotBlank @Size(max = 100) String contentType,
        @Positive long contentLength
) {}

