package com.runners.app.community.upload.dto.request;

import com.runners.app.global.validation.ValidationMessageKey;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PresignCommunityImageUploadRequest(
        @NotEmpty(message = ValidationMessageKey.UPLOAD_FILES_REQUIRED)
        @Size(max = 10, message = ValidationMessageKey.UPLOAD_TOO_MANY_FILES)
        List<@Valid PresignCommunityImageUploadFileRequest> files
) {}
