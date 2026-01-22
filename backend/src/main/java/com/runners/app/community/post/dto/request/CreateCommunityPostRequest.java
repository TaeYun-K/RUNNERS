package com.runners.app.community.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateCommunityPostRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String content,
        @Size(max = 10) List<@NotBlank @Size(max = 512) String> imageKeys
) {}
