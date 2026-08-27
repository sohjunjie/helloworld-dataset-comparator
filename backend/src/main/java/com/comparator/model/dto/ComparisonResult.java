package com.comparator.model.dto;

public record ComparisonResult(
        long ds1RecordCount,
        long ds2RecordCount,
        long ds1FullyMatching,
        long ds2FullyMatching,
        long ds1NotMatching,
        long ds2NotMatching,
        long ds1MissingInDs2,
        long ds2MissingInDs1
) {}
