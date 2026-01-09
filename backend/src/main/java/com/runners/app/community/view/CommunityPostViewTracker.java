package com.runners.app.community.view;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CommunityPostViewTracker {

    private static final String KEY_PREFIX = "community:post:view:";
    private static final String USER_TODAY_SET_PREFIX = "community:user:viewed:today:";

    private final StringRedisTemplate redis;

    public CommunityPostViewTracker(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /**
     * @return true if this is the first view for (post,user) today.
     */
    public boolean markViewedTodayIfFirst(Long postId, Long userId) {
        LocalDate today = LocalDate.now();
        String key = KEY_PREFIX + postId + ":" + userId + ":" + today;
        String userTodaySetKey = USER_TODAY_SET_PREFIX + userId + ":" + today;

        LocalDateTime now = LocalDateTime.now();
        Duration ttlUntilTomorrow = Duration.between(now, today.plusDays(1).atStartOfDay());
        if (ttlUntilTomorrow.isNegative() || ttlUntilTomorrow.isZero()) {
            ttlUntilTomorrow = Duration.ofDays(1);
        }

        Boolean inserted = redis.opsForValue().setIfAbsent(key, "1", ttlUntilTomorrow);
        boolean first = Boolean.TRUE.equals(inserted);
        if (first) {
            redis.opsForSet().add(userTodaySetKey, String.valueOf(postId));
            redis.expire(userTodaySetKey, ttlUntilTomorrow);
        }
        return first;
    }

    public boolean hasViewedToday(Long postId, Long userId) {
        LocalDate today = LocalDate.now();
        String userTodaySetKey = USER_TODAY_SET_PREFIX + userId + ":" + today;
        Boolean member = redis.opsForSet().isMember(userTodaySetKey, String.valueOf(postId));
        return Boolean.TRUE.equals(member);
    }

    public Set<Long> getViewedPostIdsToday(Long userId) {
        LocalDate today = LocalDate.now();
        String userTodaySetKey = USER_TODAY_SET_PREFIX + userId + ":" + today;
        Set<String> members = redis.opsForSet().members(userTodaySetKey);
        if (members == null || members.isEmpty()) return Collections.emptySet();
        return members.stream().map(Long::parseLong).collect(Collectors.toSet());
    }
}
