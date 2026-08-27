package com.comparator.model.dto;

import java.util.Map;

public record MissingDetail(
        Map<String, Object> keyValues,
        Map<String, Object> values,
        String missingFrom
) {}
