package com.comparator.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ToleranceConfig(
        @NotBlank(message = "Column name is required")
        String columnName,

        @NotNull(message = "Tolerance percentage is required")
        @DecimalMin(value = "0.0", message = "Tolerance percentage must be non-negative")
        Double percentage
) {}
