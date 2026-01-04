package com.runners.app.auth.controller;

import com.runners.app.auth.dto.GoogleLoginRequest;
import com.runners.app.auth.dto.GoogleLoginResponse;
import com.runners.app.auth.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/google")
    public GoogleLoginResponse googleLogin(@RequestBody GoogleLoginRequest req) {
        if (req.idToken() == null || req.idToken().isBlank()) {
            throw new IllegalArgumentException("idToken is required");
        }
        return authService.googleLogin(req.idToken());
    }
}
