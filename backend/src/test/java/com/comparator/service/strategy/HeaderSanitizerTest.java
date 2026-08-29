package com.comparator.service.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeaderSanitizerTest {

    @Test
    @DisplayName("Should sanitize normal unique headers")
    void shouldSanitizeNormalHeaders() {
        List<String> raw = List.of("id", "name", "age");
        List<String> sanitized = HeaderSanitizer.sanitize(raw);
        assertThat(sanitized).containsExactly("id", "name", "age");
    }

    @Test
    @DisplayName("Should trim whitespace and disambiguate duplicates")
    void shouldTrimAndDisambiguateDuplicates() {
        List<String> raw = List.of(" id ", "name", "id", "name");
        List<String> sanitized = HeaderSanitizer.sanitize(raw);
        assertThat(sanitized).containsExactly("id", "name", "id_1", "name_1");
    }

    @Test
    @DisplayName("Should replace empty headers before last non-empty header with column_N")
    void shouldFillEmptyHeaders() {
        List<String> raw = List.of("id", "", "score", "");
        List<String> sanitized = HeaderSanitizer.sanitize(raw);
        assertThat(sanitized).containsExactly("id", "column_2", "score");
    }

    @Test
    @DisplayName("Should return empty list for null or completely empty header list")
    void shouldHandleNullOrEmpty() {
        assertThat(HeaderSanitizer.sanitize(null)).isEmpty();
        assertThat(HeaderSanitizer.sanitize(List.of())).isEmpty();
        assertThat(HeaderSanitizer.sanitize(List.of("", "  ", "   "))).isEmpty();
    }
}
