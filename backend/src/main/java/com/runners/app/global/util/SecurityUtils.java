package com.runners.app.global.util;

import com.runners.app.auth.exception.AuthDomainException;
import org.springframework.security.core.Authentication;

public class SecurityUtils {

    private SecurityUtils() {}

    public static Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw AuthDomainException.unauthorized();
        }

        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            throw AuthDomainException.invalidTokenSubject();
        }
    }
}
