package com.runners.app.global.util;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * 커서 기반 페이지네이션용 인코딩/디코딩 유틸
 * 형식: Base64(createdAt|id)
 */
public final class CursorUtils {

    private CursorUtils() {
    }

    /**
     * createdAt, id를 담는 불변 객체
     */
    public record Cursor(LocalDateTime createdAt, long id) {
    }

    /**
     * 커서 문자열을 파싱하여 Cursor로 반환.
     * null/blank/"null"이면 null 반환.
     *
     * @throws IllegalArgumentException 잘못된 커서 형식
     */
    public static Cursor decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank() || "null".equalsIgnoreCase(cursor)) {
            return null;
        }

        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid cursor");
            }
            LocalDateTime createdAt = LocalDateTime.parse(parts[0]);
            long id = Long.parseLong(parts[1]);
            if (id <= 0) {
                throw new IllegalArgumentException("Invalid cursor");
            }
            return new Cursor(createdAt, id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid cursor", e);
        }
    }

    /**
     * createdAt, id를 커서 문자열로 인코딩
     */
    public static String encodeCursor(LocalDateTime createdAt, Long id) {
        String raw = createdAt.toString() + "|" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}
