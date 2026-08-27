package com.comparator.model.dto;

import com.comparator.model.entity.ComparisonRecord;
import com.comparator.model.enums.ComparisonStatus;
import com.comparator.model.enums.DataSourceType;

import java.time.LocalDateTime;

public record ComparisonSummary(
        String id,
        ComparisonStatus status,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        DataSourceType ds1Type,
        String ds1FileName,
        DataSourceType ds2Type,
        String ds2FileName,
        String configJson,
        Long ds1RecordCount,
        Long ds2RecordCount,
        Long ds1FullyMatching,
        Long ds2FullyMatching,
        Long ds1NotMatching,
        Long ds2NotMatching,
        Long ds1MissingInDs2,
        Long ds2MissingInDs1,
        String errorMessage
) {
    public static ComparisonSummary fromEntity(ComparisonRecord entity) {
        if (entity == null) {
            return null;
        }
        return new ComparisonSummary(
                entity.getId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getCompletedAt(),
                entity.getDs1Type(),
                entity.getDs1FileName(),
                entity.getDs2Type(),
                entity.getDs2FileName(),
                entity.getConfigJson(),
                entity.getDs1RecordCount(),
                entity.getDs2RecordCount(),
                entity.getDs1FullyMatching(),
                entity.getDs2FullyMatching(),
                entity.getDs1NotMatching(),
                entity.getDs2NotMatching(),
                entity.getDs1MissingInDs2(),
                entity.getDs2MissingInDs1(),
                entity.getErrorMessage()
        );
    }
}
