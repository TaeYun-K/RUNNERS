package com.runners.app.auth.service;

import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    private static final String USER_KEY_PREFIX = "refresh:user:";

    private final StringRedisTemplate redis;

    public RefreshTokenService(@Qualifier("refreshStringRedisTemplate") StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void save(Long userId, String refreshToken, Duration ttl) {
        String userKey = USER_KEY_PREFIX + userId;
        redis.opsForValue().set(userKey, refreshToken, ttl);
    }

    public Optional<String> findTokenByUserId(Long userId) {
        String token = redis.opsForValue().get(USER_KEY_PREFIX + userId);
        if (token == null || token.isBlank()) return Optional.empty();
        return Optional.of(token);
    }

    public void deleteByUserId(Long userId) {
        redis.delete(USER_KEY_PREFIX + userId);
    }
}
