package com.comparator.model.dto;

import java.util.List;
import java.util.Map;

public record MismatchDetail(
        Map<String, Object> keyValues,
        Map<String, Object> ds1Values,
        Map<String, Object> ds2Values,
        List<String> mismatchedColumns
) {}
