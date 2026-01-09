package com.runners.app.user.controller;

import com.runners.app.user.dto.UpdateNicknameRequest;
import com.runners.app.user.dto.UpdateTotalDistanceRequest;
import com.runners.app.user.dto.UserMeResponse;
import com.runners.app.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Operation(summary = "내 정보 조회", description = "JWT로 인증된 사용자 정보를 반환")
    @GetMapping("/me")
    public UserMeResponse me(Authentication authentication) {
        Long userId = requireUserId(authentication);

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return new UserMeResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getPicture(),
                user.getRole(),
                user.getTotalDistanceKm()
        );
    }

    @Operation(summary = "닉네임 변경", description = "커뮤니티/앱에서 사용할 닉네임을 변경합니다.")
    @PatchMapping("/me/nickname")
    public UserMeResponse updateNickname(
            Authentication authentication,
            @Valid @RequestBody UpdateNicknameRequest request
    ) {
        Long userId = requireUserId(authentication);

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String newNickname = request.nickname().trim();
        if (newNickname.equals(user.getNickname())) {
            return new UserMeResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getNickname(),
                    user.getPicture(),
                    user.getRole(),
                    user.getTotalDistanceKm()
            );
        }

        if (userRepository.existsByNickname(newNickname)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nickname already in use");
        }

        user.updateNickname(newNickname);
        userRepository.save(user);

        return new UserMeResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getPicture(),
                user.getRole(),
                user.getTotalDistanceKm()
        );
    }

    @Operation(summary = "내 누적 거리 업데이트", description = "사용자의 누적 러닝 거리(km)를 업데이트합니다.")
    @PatchMapping("/me/total-distance")
    public UserMeResponse updateTotalDistanceKm(
            Authentication authentication,
            @Valid @RequestBody UpdateTotalDistanceRequest request
    ) {
        Long userId = requireUserId(authentication);

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Double nextKm = request.totalDistanceKm();
        if (Objects.equals(user.getTotalDistanceKm(), nextKm)) {
            return new UserMeResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getNickname(),
                    user.getPicture(),
                    user.getRole(),
                    user.getTotalDistanceKm()
            );
        }

        user.updateTotalDistanceKm(nextKm);
        userRepository.save(user);

        return new UserMeResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getPicture(),
                user.getRole(),
                user.getTotalDistanceKm()
        );
    }

    private static Long requireUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token subject");
        }
    }
}
