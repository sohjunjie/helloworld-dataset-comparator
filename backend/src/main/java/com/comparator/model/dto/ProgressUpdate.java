package com.comparator.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProgressUpdate(
        String stage,
        int percent,
        String message
) {
    public ProgressUpdate(String stage, int percent) {
        this(stage, percent, null);
    }
}
