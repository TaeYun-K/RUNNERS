package com.runners.app.user.controller;

import com.runners.app.user.dto.UpdateNicknameRequest;
import com.runners.app.user.dto.UpdateTotalDistanceRequest;
import com.runners.app.user.dto.UserMeResponse;
import com.runners.app.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
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

    @Operation(summary = "닉네임 변경", description = "커뮤니티/앱에서 사용할 닉네임을 변경합니다.")
    @PatchMapping("/me/nickname")
    public UserMeResponse updateNickname(
            Authentication authentication,
            @Valid @RequestBody UpdateNicknameRequest request
    ) {
        Long userId = SecurityUtils.extractUserId(authentication);
        return userService.updateNickname(userId, request.nickname());
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
}
