package com.comparator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        StorageProperties storage,
        UploadProperties upload,
        CleanupProperties cleanup,
        ComparisonProperties comparison
) {
    public record StorageProperties(
            @DefaultValue("./data") String path
    ) {}

    public record UploadProperties(
            @DefaultValue("500MB") String maxFileSize
    ) {}

    public record CleanupProperties(
            @DefaultValue("1") int ttlHours
    ) {}

    public record ComparisonProperties(
            @DefaultValue("30") int timeoutMinutes
    ) {}
}
