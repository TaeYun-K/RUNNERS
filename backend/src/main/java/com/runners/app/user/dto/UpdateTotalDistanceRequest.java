package com.runners.app.user.dto;

import com.runners.app.global.validation.ValidationMessageKey;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record UpdateTotalDistanceRequest(
        @NotNull(message = ValidationMessageKey.TOTAL_DISTANCE_REQUIRED)
        @DecimalMin(value = "0.0", inclusive = true, message = ValidationMessageKey.TOTAL_DISTANCE_OUT_OF_RANGE)
        @DecimalMax(value = "1000000.0", inclusive = true, message = ValidationMessageKey.TOTAL_DISTANCE_OUT_OF_RANGE)
        Double totalDistanceKm
) {}
