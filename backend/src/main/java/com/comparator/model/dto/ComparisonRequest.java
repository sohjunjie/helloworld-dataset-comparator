package com.comparator.model.dto;

import com.comparator.model.enums.DataSourceType;
import jakarta.validation.Valid;

import java.util.List;

public record ComparisonRequest(
        DataSourceType ds1Type,
        String ds1FileName,
        DataSourceType ds2Type,
        String ds2FileName,
        List<String> keyColumns,
        @Valid List<ToleranceConfig> tolerances,
        Boolean caseSensitive,
        @Valid DatabaseConnectionConfig ds1Connection,
        @Valid DatabaseConnectionConfig ds2Connection,
        String ds1Sql,
        String ds2Sql,
        String ds1Delimiter,
        String ds2Delimiter
) {}
