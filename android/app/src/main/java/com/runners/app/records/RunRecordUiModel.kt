package com.runners.app.records

import java.time.Instant
import java.time.LocalDate

data class RunRecordUiModel(
    val date: LocalDate,
    val startTime: Instant,
    val endTime: Instant,
    val dataOriginPackageName: String,
    val distanceKm: Double,
    val durationMinutes: Long?,
)
