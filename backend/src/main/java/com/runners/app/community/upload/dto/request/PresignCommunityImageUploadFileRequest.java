package com.runners.app.community.upload.dto.request;

import com.runners.app.global.validation.ValidationMessageKey;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PresignCommunityImageUploadFileRequest(
        @Size(max = 255, message = ValidationMessageKey.UPLOAD_FILE_NAME_TOO_LONG) String fileName,
        @NotBlank(message = ValidationMessageKey.UPLOAD_CONTENT_TYPE_REQUIRED)
        @Size(max = 100, message = ValidationMessageKey.UPLOAD_CONTENT_TYPE_TOO_LONG)
        String contentType,
        @Positive(message = ValidationMessageKey.UPLOAD_CONTENT_LENGTH_INVALID) long contentLength
) {}
