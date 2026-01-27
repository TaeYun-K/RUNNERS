package com.runners.app.community.post.dto.request;

import com.runners.app.community.post.entity.CommunityPostBoardType;
import com.runners.app.global.validation.ValidationMessageKey;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateCommunityPostRequest(
        @NotBlank(message = ValidationMessageKey.TITLE_REQUIRED)
        @Size(max = 200, message = ValidationMessageKey.TITLE_TOO_LONG)
        String title,
        @NotBlank(message = ValidationMessageKey.CONTENT_REQUIRED) String content,
        @Size(max = 10, message = ValidationMessageKey.IMAGE_TOO_MANY)
        List<@NotBlank(message = ValidationMessageKey.IMAGE_KEY_REQUIRED) @Size(max = 512, message = ValidationMessageKey.IMAGE_KEY_TOO_LONG) String> imageKeys,
        CommunityPostBoardType boardType
) {}
