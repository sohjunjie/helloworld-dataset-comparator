package com.comparator.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UploadConfigRequest(
        String ds1Delimiter,
        String ds2Delimiter,
        String ds1Sql,
        DatabaseConnectionConfig ds1Connection,
        String ds2Sql,
        DatabaseConnectionConfig ds2Connection
) {
    public UploadConfigRequest() {
        this("auto", "auto", null, null, null, null);
    }

    public UploadConfigRequest(String ds1Delimiter, String ds2Delimiter) {
        this(ds1Delimiter, ds2Delimiter, null, null, null, null);
    }

    public String getDs1DelimiterOrDefault() {
        return (ds1Delimiter != null && !ds1Delimiter.isBlank()) ? ds1Delimiter : "auto";
    }

    public String getDs2DelimiterOrDefault() {
        return (ds2Delimiter != null && !ds2Delimiter.isBlank()) ? ds2Delimiter : "auto";
    }

    public static String resolveDs1Delimiter(UploadConfigRequest config, String fallbackParam) {
        if (config != null && config.ds1Delimiter() != null && !config.ds1Delimiter().isBlank()) {
            return config.ds1Delimiter();
        }
        if (fallbackParam != null && !fallbackParam.isBlank()) {
            return fallbackParam;
        }
        return "auto";
    }

    public static String resolveDs2Delimiter(UploadConfigRequest config, String fallbackParam) {
        if (config != null && config.ds2Delimiter() != null && !config.ds2Delimiter().isBlank()) {
            return config.ds2Delimiter();
        }
        if (fallbackParam != null && !fallbackParam.isBlank()) {
            return fallbackParam;
        }
        return "auto";
    }
}
