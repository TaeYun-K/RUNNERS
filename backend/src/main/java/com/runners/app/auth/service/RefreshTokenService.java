package com.runners.app.auth.service;

import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    private static final String USER_KEY_PREFIX = "refresh:user:";
    private static final String TOKEN_KEY_PREFIX = "refresh:token:";

    private final StringRedisTemplate redis;

    public RefreshTokenService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void save(Long userId, String refreshToken, Duration ttl) {
        String userKey = USER_KEY_PREFIX + userId;
        String tokenKey = TOKEN_KEY_PREFIX + refreshToken;

        String previousToken = redis.opsForValue().get(userKey);
        if (previousToken != null && !previousToken.isBlank()) {
            redis.delete(TOKEN_KEY_PREFIX + previousToken);
        }

        redis.opsForValue().set(userKey, refreshToken, ttl);
        redis.opsForValue().set(tokenKey, String.valueOf(userId), ttl);
    }

    public Optional<Long> findUserIdByToken(String refreshToken) {
        String value = redis.opsForValue().get(TOKEN_KEY_PREFIX + refreshToken);
        if (value == null || value.isBlank()) return Optional.empty();
        try {
            return Optional.of(Long.parseLong(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public void deleteByUserId(Long userId) {
        String userKey = USER_KEY_PREFIX + userId;
        String token = redis.opsForValue().get(userKey);
        redis.delete(userKey);
        if (token != null && !token.isBlank()) {
            redis.delete(TOKEN_KEY_PREFIX + token);
        }
    }

    public void deleteByToken(String refreshToken) {
        String tokenKey = TOKEN_KEY_PREFIX + refreshToken;
        String userId = redis.opsForValue().get(tokenKey);
        redis.delete(tokenKey);
        if (userId != null && !userId.isBlank()) {
            redis.delete(USER_KEY_PREFIX + userId);
        }
    }
}
