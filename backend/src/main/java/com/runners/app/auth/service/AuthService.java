package com.runners.app.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.runners.app.auth.dto.GoogleLoginResponse;
import com.runners.app.auth.dto.TokenRefreshResponse;
import com.runners.app.user.entity.User;
import com.runners.app.user.repository.UserRepository;
import com.runners.app.user.service.UserProfileImageResolver;
import com.runners.app.user.service.NicknameService;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class AuthService {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final NicknameService nicknameService;
    private final UserProfileImageResolver userProfileImageResolver;

    public AuthService(
            GoogleTokenVerifier googleTokenVerifier,
            UserRepository userRepository,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            NicknameService nicknameService,
            UserProfileImageResolver userProfileImageResolver
    ) {
        this.googleTokenVerifier = googleTokenVerifier;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.nicknameService = nicknameService;
        this.userProfileImageResolver = userProfileImageResolver;
    }

    @Transactional
    public GoogleLoginResponse googleLogin(String idToken) {
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(idToken);

        String email = payload.getEmail();
        String sub = payload.getSubject();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        // 1) googleSub 우선 조회 (가장 정확)
        var existingBySub = userRepository.findByGoogleSub(sub);
        if (existingBySub.isPresent()) {
            User user = existingBySub.get();
            user.updateProfile(name, picture);
            ensureNickname(user);
            // 닉네임/프로필 변경이 트랜잭션에 반영되도록 명시적으로 save
            userRepository.save(user);
            String accessToken = jwtService.createAccessToken(user);
            String refreshToken = jwtService.createRefreshToken(user);
            refreshTokenService.save(user.getId(), refreshToken, jwtService.refreshTokenTtl());
            return new GoogleLoginResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getNickname(),
                    userProfileImageResolver.resolve(user),
                    accessToken,
                    refreshToken,
                    false
            );
        }

        // 2) email로도 조회 (기존에 email로 가입했을 가능성 대비)
        var existingByEmail = userRepository.findByEmail(email);
        if (existingByEmail.isPresent()) {
            User user = existingByEmail.get();
            // 구글 로그인으로 연결(연동)
            // (간단히 googleSub 업데이트하는 방식)
            user.updateProfile(name, picture);
            // googleSub 컬럼이 nullable=false라서, 여기서는 "연동"을 위해 엔티티에 setter 또는 메서드 추가 필요
            // 가장 깔끔한 방법: googleSub를 업데이트하는 도메인 메서드 추가
            userRepository.save(user); // 변경감지로도 OK
            // ⚠️ 이 케이스는 설계 선택: email 가입 + google 연동을 허용할지 여부에 따라 달라짐
            ensureNickname(user);
            // ensureNickname에서 닉네임이 채워진 경우도 DB에 확실히 반영
            userRepository.save(user);
            String accessToken = jwtService.createAccessToken(user);
            String refreshToken = jwtService.createRefreshToken(user);
            refreshTokenService.save(user.getId(), refreshToken, jwtService.refreshTokenTtl());
            return new GoogleLoginResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getNickname(),
                    userProfileImageResolver.resolve(user),
                    accessToken,
                    refreshToken,
                    false
            );
        }

        // 3) 신규 가입
        User newUser = User.builder()
                .email(email)
                .googleSub(sub)
                .name(name)
                .nickname(nicknameService.generateUniqueNickname())
                .picture(picture)
                .role("USER")
                .build();

        User saved = userRepository.save(newUser);
        String accessToken = jwtService.createAccessToken(saved);
        String refreshToken = jwtService.createRefreshToken(saved);
        refreshTokenService.save(saved.getId(), refreshToken, jwtService.refreshTokenTtl());
        return new GoogleLoginResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getName(),
                saved.getNickname(),
                userProfileImageResolver.resolve(saved),
                accessToken,
                refreshToken,
                true
        );
    }

    private void ensureNickname(User user) {
        if (user.getNickname() != null && !user.getNickname().isBlank()) return;
        user.updateNickname(nicknameService.generateUniqueNickname());
    }

    @Transactional(readOnly = true)
    public TokenRefreshResponse refreshAccessToken(String refreshToken) {
        Claims claims;
        try {
            claims = jwtService.parseAndValidate(refreshToken);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        String type = claims.get("type", String.class);
        if (!"refresh".equals(type)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        String subject = claims.getSubject();
        Long userIdFromJwt;
        try {
            userIdFromJwt = Long.parseLong(subject);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        String storedToken = refreshTokenService.findTokenByUserId(userIdFromJwt)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (!refreshToken.equals(storedToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        User user = userRepository.findById(userIdFromJwt)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        String accessToken = jwtService.createAccessToken(user);
        return new TokenRefreshResponse(accessToken);
    }
}
