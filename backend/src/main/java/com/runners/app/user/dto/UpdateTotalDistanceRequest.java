package com.runners.app.user.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record UpdateTotalDistanceRequest(
        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        @DecimalMax(value = "1000000.0", inclusive = true)
        Double totalDistanceKm
) {}

