package com.runners.app.user.dto;

import com.runners.app.global.validation.ValidationMessageKey;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateRunningStatsRequest(
        @NotNull(message = ValidationMessageKey.TOTAL_DISTANCE_REQUIRED)
        @DecimalMin(value = "0.0", inclusive = true, message = ValidationMessageKey.TOTAL_DISTANCE_OUT_OF_RANGE)
        @DecimalMax(value = "1000000.0", inclusive = true, message = ValidationMessageKey.TOTAL_DISTANCE_OUT_OF_RANGE)
        Double totalDistanceKm,

        @NotNull(message = ValidationMessageKey.TOTAL_DURATION_REQUIRED)
        @Min(value = 0, message = ValidationMessageKey.TOTAL_DURATION_OUT_OF_RANGE)
        @Max(value = 1000000000, message = ValidationMessageKey.TOTAL_DURATION_OUT_OF_RANGE)
        Long totalDurationMinutes,

        @NotNull(message = ValidationMessageKey.RUN_COUNT_REQUIRED)
        @Min(value = 0, message = ValidationMessageKey.RUN_COUNT_OUT_OF_RANGE)
        @Max(value = 1000000000, message = ValidationMessageKey.RUN_COUNT_OUT_OF_RANGE)
        Integer runCount
) {}
