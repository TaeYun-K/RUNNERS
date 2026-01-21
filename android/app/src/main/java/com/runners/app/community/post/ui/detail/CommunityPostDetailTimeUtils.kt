package com.runners.app.community.post.ui.detail

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

private const val EDITED_BADGE_THRESHOLD_SECONDS = 60L

internal fun toSecondPrecision(raw: String): String {
    val normalized = raw.replace('T', ' ')
    // Show up to minute precision: "yyyy-MM-dd HH:mm"
    return normalized.takeIf { it.length >= 16 }?.substring(0, 16) ?: normalized
}

internal fun shouldShowEditedBadge(createdAt: String, updatedAt: String?): Boolean {
    if (updatedAt.isNullOrBlank()) return false

    fun parseInstantOrNull(raw: String): Instant? {
        val text = raw.trim().replace(' ', 'T')
        return runCatching { OffsetDateTime.parse(text).toInstant() }.getOrNull()
            ?: runCatching { LocalDateTime.parse(text).atZone(ZoneId.systemDefault()).toInstant() }.getOrNull()
    }

    val createdInstant = parseInstantOrNull(createdAt)
    val updatedInstant = parseInstantOrNull(updatedAt)
    if (createdInstant == null || updatedInstant == null) {
        // Best-effort fallback: match what we show in UI (minute precision).
        return toSecondPrecision(createdAt) != toSecondPrecision(updatedAt)
    }

    val deltaSeconds = Duration.between(createdInstant, updatedInstant).seconds
    return deltaSeconds >= EDITED_BADGE_THRESHOLD_SECONDS
}
