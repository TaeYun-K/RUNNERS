package com.runners.app.auth.service;

import com.runners.app.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey key;
    private final Duration accessTokenTtl;
    private final Duration refreshTokenTtl;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-minutes:60}") long accessTokenMinutes,
            @Value("${jwt.refresh-token-days:14}") long refreshTokenDays
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtl = Duration.ofMinutes(accessTokenMinutes);
        this.refreshTokenTtl = Duration.ofDays(refreshTokenDays);
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plus(accessTokenTtl);

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plus(refreshTokenTtl);

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("type", "refresh")
                .signWith(key)
                .compact();
    }

    public Duration refreshTokenTtl() {
        return refreshTokenTtl;
    }

    public Claims parseAndValidate(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Authentication toAuthentication(Claims claims) {
        String userId = claims.getSubject();
        String role = claims.get("role", String.class);
        String normalizedRole = (role == null || role.isBlank()) ? "USER" : role;
        String authority = normalizedRole.startsWith("ROLE_") ? normalizedRole : ("ROLE_" + normalizedRole);

        return new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority(authority))
        );
    }
}
