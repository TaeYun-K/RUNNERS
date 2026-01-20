package com.runners.app.community.post.ui.detail

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

internal fun toSecondPrecision(raw: String): String {
    val normalized = raw.replace('T', ' ')
    return normalized.takeIf { it.length >= 19 }?.substring(0, 19) ?: normalized
}

internal fun shouldShowEditedBadge(createdAt: String, updatedAt: String?): Boolean {
    if (updatedAt.isNullOrBlank()) return false

    fun parseInstantOrNull(raw: String): Instant? {
        val text = raw.trim().replace(' ', 'T')
        return runCatching { OffsetDateTime.parse(text).toInstant() }.getOrNull()
            ?: runCatching { LocalDateTime.parse(text).atZone(ZoneId.systemDefault()).toInstant() }.getOrNull()
    }

    val createdInstant = parseInstantOrNull(createdAt) ?: return true
    val updatedInstant = parseInstantOrNull(updatedAt) ?: return true
    val deltaSeconds = Duration.between(createdInstant, updatedInstant).seconds
    return deltaSeconds > 0
}

