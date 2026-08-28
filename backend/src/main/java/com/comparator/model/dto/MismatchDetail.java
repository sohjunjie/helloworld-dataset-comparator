package com.comparator.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MismatchDetail(
        Long rowNumber,
        Map<String, Object> keyValues,
        @JsonProperty("ds1Values") Map<String, Object> ds1Values,
        @JsonProperty("ds2Values") Map<String, Object> ds2Values,
        @JsonProperty("differingColumns") List<String> differingColumns
) {
    public MismatchDetail(
            Map<String, Object> keyValues,
            Map<String, Object> ds1Values,
            Map<String, Object> ds2Values,
            List<String> differingColumns
    ) {
        this(null, keyValues, ds1Values, ds2Values, differingColumns);
    }

    @JsonProperty("dataDs1")
    public Map<String, Object> getDataDs1() {
        return ds1Values;
    }

    @JsonProperty("dataDs2")
    public Map<String, Object> getDataDs2() {
        return ds2Values;
    }

    @JsonProperty("mismatchedColumns")
    public List<String> getMismatchedColumns() {
        return differingColumns;
    }

    @JsonProperty("rowNumberDs1")
    public Long getRowNumberDs1() {
        return rowNumber;
    }

    @JsonProperty("rowNumberDs2")
    public Long getRowNumberDs2() {
        return rowNumber;
    }
}
