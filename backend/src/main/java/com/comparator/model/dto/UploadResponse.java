package com.comparator.model.dto;

public record UploadResponse(
        String comparisonId,
        DatasetColumns columns
) {
}
