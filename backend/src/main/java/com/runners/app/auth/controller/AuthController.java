package com.runners.app.auth.controller;

import com.runners.app.auth.dto.GoogleLoginRequest;
import com.runners.app.auth.dto.GoogleLoginResponse;
import com.runners.app.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @Operation(summary = "Google 로그인", description = "Android에서 받은 idToken을 검증하고 사용자 정보를 반환")
    @PostMapping("/google")
    public GoogleLoginResponse googleLogin(@RequestBody GoogleLoginRequest req) {
        if (req.idToken() == null || req.idToken().isBlank()) {
            throw new IllegalArgumentException("idToken is required");
        }
        return authService.googleLogin(req.idToken());
    }
}
