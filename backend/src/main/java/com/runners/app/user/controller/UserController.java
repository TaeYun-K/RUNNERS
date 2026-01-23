package com.runners.app.user.controller;

import com.runners.app.user.dto.UpdateProfileRequest;
import com.runners.app.user.dto.UpdateRunningStatsRequest;
import com.runners.app.user.dto.UpdateTotalDistanceRequest;
import com.runners.app.user.dto.UserMeResponse;
import com.runners.app.user.dto.UserPublicProfileResponse;
import com.runners.app.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.runners.app.global.util.SecurityUtils;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "내 정보 조회", description = "JWT로 인증된 사용자 정보를 반환")
    @GetMapping("/me")
    public UserMeResponse me(Authentication authentication) {
        Long userId = SecurityUtils.extractUserId(authentication);
        return userService.getMe(userId);
    }

    @Operation(summary = "유저 공개 프로필 조회", description = "커뮤니티 등에서 다른 사용자의 프로필을 조회합니다.")
    @GetMapping("/{userId}/public-profile")
    public UserPublicProfileResponse publicProfile(@PathVariable Long userId) {
        return userService.getPublicProfile(userId);
    }

    @Operation(summary = "내 프로필 수정", description = "닉네임/한줄 소개 등 프로필 정보를 수정합니다. (부분 수정)")
    @PatchMapping("/me/profile")
    public UserMeResponse updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        Long userId = SecurityUtils.extractUserId(authentication);
        return userService.updateProfile(userId, request.nickname(), request.intro());
    }

    @Operation(summary = "내 누적 거리 업데이트", description = "사용자의 누적 러닝 거리(km)를 업데이트합니다.")
    @PatchMapping("/me/total-distance")
    public UserMeResponse updateTotalDistanceKm(
            Authentication authentication,
            @Valid @RequestBody UpdateTotalDistanceRequest request
    ) {
        Long userId = SecurityUtils.extractUserId(authentication);
        return userService.updateTotalDistanceKm(userId, request.totalDistanceKm());
    }

    @Operation(summary = "내 러닝 통계 업데이트", description = "대시보드 요약(누적거리/총시간/횟수) 정보를 업데이트합니다.")
    @PatchMapping("/me/running-stats")
    public UserMeResponse updateRunningStats(
            Authentication authentication,
            @Valid @RequestBody UpdateRunningStatsRequest request
    ) {
        Long userId = SecurityUtils.extractUserId(authentication);
        return userService.updateRunningStats(userId, request.totalDistanceKm(), request.totalDurationMinutes(), request.runCount());
    }
}
