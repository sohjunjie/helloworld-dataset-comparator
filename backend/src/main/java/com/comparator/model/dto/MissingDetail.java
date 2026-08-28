package com.comparator.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MissingDetail(
        Long rowNumber,
        Map<String, Object> keyValues,
        @JsonProperty("values") Map<String, Object> values,
        @JsonProperty("missingFrom") String missingFrom,
        @JsonProperty("direction") String direction
) {
    public MissingDetail(
            Map<String, Object> keyValues,
            Map<String, Object> values,
            String missingFrom
    ) {
        this(null, keyValues, values, missingFrom, null);
    }

    @JsonProperty("data")
    public Map<String, Object> getData() {
        return values;
    }
}
