package com.runners.app.user.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateRunningStatsRequest(
        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        @DecimalMax(value = "1000000.0", inclusive = true)
        Double totalDistanceKm,

        @NotNull
        @Min(value = 0)
        @Max(value = 1000000000)
        Long totalDurationMinutes,

        @NotNull
        @Min(value = 0)
        @Max(value = 1000000000)
        Integer runCount
) {}

