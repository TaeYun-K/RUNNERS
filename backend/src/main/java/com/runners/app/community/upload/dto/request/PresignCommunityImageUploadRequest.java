package com.runners.app.community.upload.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PresignCommunityImageUploadRequest(
        @NotEmpty @Size(max = 10) List<@Valid PresignCommunityImageUploadFileRequest> files
) {}

