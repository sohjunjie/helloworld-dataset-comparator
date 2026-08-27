package com.comparator.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DatabaseConnectionConfig(
        @NotBlank(message = "Host is required")
        String host,

        @NotNull(message = "Port is required")
        @Min(value = 1, message = "Port must be at least 1")
        @Max(value = 65535, message = "Port must be at most 65535")
        Integer port,

        @NotBlank(message = "Database name is required")
        String database,

        @NotBlank(message = "Username is required")
        String username,

        String password
) {}
