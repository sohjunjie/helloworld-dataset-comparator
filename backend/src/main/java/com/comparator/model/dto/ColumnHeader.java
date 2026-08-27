package com.comparator.model.dto;

import jakarta.validation.constraints.NotBlank;

public record ColumnHeader(
        @NotBlank(message = "Column name is required")
        String name,

        String dataType
) {
    public ColumnHeader(String name) {
        this(name, "STRING");
    }
}
