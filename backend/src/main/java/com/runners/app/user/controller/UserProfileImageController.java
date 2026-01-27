package com.runners.app.user.controller;

import com.runners.app.community.upload.dto.request.PresignCommunityImageUploadRequest;
import com.runners.app.community.upload.dto.response.PresignCommunityImageUploadResponse;
import com.runners.app.global.util.SecurityUtils;
import com.runners.app.user.dto.UpdateProfileImageRequest;
import com.runners.app.user.dto.UserMeResponse;
import com.runners.app.user.exception.UserDomainException;
import com.runners.app.user.service.UserService;
import com.runners.app.community.upload.service.CommunityUploadService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me/profile-image")
public class UserProfileImageController {

    private final CommunityUploadService communityUploadService;
    private final UserService userService;

    public UserProfileImageController(
            CommunityUploadService communityUploadService,
            UserService userService
    ) {
        this.communityUploadService = communityUploadService;
        this.userService = userService;
    }

    @Operation(summary = "프로필 이미지 업로드 presigned URL 발급", description = "JWT로 인증된 사용자가 S3 PUT 업로드용 presigned URL을 발급받습니다.")
    @PostMapping("/presign")
    @ResponseStatus(HttpStatus.CREATED)
    public PresignCommunityImageUploadResponse presignProfileImageUpload(
            Authentication authentication,
            @Valid @RequestBody PresignCommunityImageUploadRequest request
    ) {
        if (request == null || request.files() == null || request.files().size() != 1) {
            throw UserDomainException.profileImageFilesCountInvalid();
        }
        Long userId = SecurityUtils.extractUserId(authentication);
        return communityUploadService.presignUserProfileImageUpload(userId, request);
    }

    @Operation(summary = "프로필 이미지 등록", description = "업로드 완료된 S3 key를 사용자 프로필 이미지로 등록합니다.")
    @PostMapping("/commit")
    public UserMeResponse commitProfileImage(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileImageRequest request
    ) {
        Long userId = SecurityUtils.extractUserId(authentication);
        return userService.updateProfileImage(userId, request.key());
    }

    @Operation(summary = "프로필 이미지 삭제", description = "사용자의 커스텀 프로필 이미지를 삭제하고 기본 이미지로 되돌립니다.")
    @DeleteMapping
    public UserMeResponse deleteProfileImage(Authentication authentication) {
        Long userId = SecurityUtils.extractUserId(authentication);
        return userService.deleteProfileImage(userId);
    }
}
