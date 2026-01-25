package com.runners.app.auth.cookie;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import org.springframework.http.ResponseCookie;

public final class RefreshTokenCookie {

    public static final String COOKIE_NAME = "refresh_token";

    private RefreshTokenCookie() {}

    public static ResponseCookie create(HttpServletRequest request, String refreshToken, Duration ttl) {
        return ResponseCookie.from(COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(isSecure(request))
                .path(cookiePath(request))
                .maxAge(ttl)
                // SameSite is a browser concept; keeping it explicit helps when the web frontend uses cookies.
                .sameSite("Lax")
                .build();
    }

    public static ResponseCookie clear(HttpServletRequest request) {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(isSecure(request))
                .path(cookiePath(request))
                .maxAge(Duration.ZERO)
                .sameSite("Lax")
                .build();
    }

    private static boolean isSecure(HttpServletRequest request) {
        if (request.isSecure()) return true;
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        return forwardedProto != null && forwardedProto.equalsIgnoreCase("https");
    }

    private static String cookiePath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        if (contextPath == null) contextPath = "";
        return contextPath + "/auth";
    }
}
