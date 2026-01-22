package com.runners.app.community.upload.controller;

import com.runners.app.community.upload.dto.request.PresignCommunityImageUploadRequest;
import com.runners.app.community.upload.dto.response.PresignCommunityImageUploadResponse;
import com.runners.app.community.upload.service.CommunityUploadService;
import com.runners.app.global.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/community/uploads")
public class CommunityUploadController {

    private final CommunityUploadService communityUploadService;

    public CommunityUploadController(CommunityUploadService communityUploadService) {
        this.communityUploadService = communityUploadService;
    }

    @Operation(summary = "게시글 이미지 업로드 presigned URL 발급", description = "JWT로 인증된 사용자가 S3 PUT 업로드용 presigned URL을 발급받습니다.")
    @PostMapping("/presign")
    @ResponseStatus(HttpStatus.CREATED)
    public PresignCommunityImageUploadResponse presignCommunityPostImageUploads(
            Authentication authentication,
            @Valid @RequestBody PresignCommunityImageUploadRequest request
    ) {
        Long userId = SecurityUtils.extractUserId(authentication);
        return communityUploadService.presignCommunityPostImageUploads(userId, request);
    }
}

