package com.runners.app.auth.controller;

import com.runners.app.auth.dto.DevTokenRequest;
import com.runners.app.auth.dto.GoogleLoginResponse;
import com.runners.app.auth.service.JwtService;
import com.runners.app.auth.service.RefreshTokenService;
import com.runners.app.user.entity.User;
import com.runners.app.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth/dev")
public class DevAuthController {

    private final boolean enabled;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public DevAuthController(
            @Value("${app.dev-auth.enabled:false}") String enabled,
            UserRepository userRepository,
            JwtService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        this.enabled = Boolean.parseBoolean(enabled);
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Operation(
            summary = "개발용 JWT 발급",
            description = "Swagger 테스트용: email 기준으로 유저를 조회(없으면 생성) 후 JWT를 발급합니다.",
            security = {}
    )
    @PostMapping("/token")
    @ResponseStatus(HttpStatus.CREATED)
    public GoogleLoginResponse issueDevToken(@Valid @RequestBody DevTokenRequest request) {
        if (!enabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }

        boolean isNewUser = false;
        User user = userRepository.findByEmail(request.email()).orElse(null);

        if (user == null) {
            isNewUser = true;
            String googleSub = "dev-" + UUID.randomUUID().toString().replace("-", "");
            String role = (request.role() == null || request.role().isBlank()) ? "USER" : request.role().trim();

            user = userRepository.save(User.builder()
                    .email(request.email())
                    .googleSub(googleSub)
                    .role(role)
                    .name(request.name())
                    .picture(null)
                    .build());
        }

        String token = jwtService.createAccessToken(user);
        String refreshToken = jwtService.createRefreshToken(user);
        refreshTokenService.save(user.getId(), refreshToken, jwtService.refreshTokenTtl());

        return new GoogleLoginResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPicture(),
                token,
                refreshToken,
                isNewUser
        );
    }
}
