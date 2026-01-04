package com.runners.app.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.runners.app.auth.dto.GoogleLoginResponse;
import com.runners.app.user.entity.User;
import com.runners.app.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(GoogleTokenVerifier googleTokenVerifier, UserRepository userRepository, JwtService jwtService) {
        this.googleTokenVerifier = googleTokenVerifier;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
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
            String accessToken = jwtService.createAccessToken(user);
            return new GoogleLoginResponse(user.getId(), user.getEmail(), user.getName(), user.getPicture(), accessToken, false);
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
            String accessToken = jwtService.createAccessToken(user);
            return new GoogleLoginResponse(user.getId(), user.getEmail(), user.getName(), user.getPicture(), accessToken, false);
        }

        // 3) 신규 가입
        User newUser = User.builder()
                .email(email)
                .googleSub(sub)
                .name(name)
                .picture(picture)
                .role("USER")
                .build();

        User saved = userRepository.save(newUser);
        String accessToken = jwtService.createAccessToken(saved);
        return new GoogleLoginResponse(saved.getId(), saved.getEmail(), saved.getName(), saved.getPicture(), accessToken, true);
    }
}
