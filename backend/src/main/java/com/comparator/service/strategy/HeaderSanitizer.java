package com.comparator.service.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HeaderSanitizer {

    private HeaderSanitizer() {
    }

    /**
     * Removes leading Byte Order Mark (BOM) characters from a string if present.
     *
     * @param value the raw string
     * @return string stripped of leading BOM characters, or null if input is null
     */
    public static String stripBom(String value) {
        if (value == null) {
            return null;
        }
        while (!value.isEmpty() && (value.charAt(0) == '\uFEFF' || value.charAt(0) == '\uFFFE')) {
            value = value.substring(1);
        }
        return value;
    }

    /**
     * Sanitizes raw header names: strips BOM, trims whitespace, fills empty header names with column_<index>,
     * and disambiguates duplicate header names by appending _<count>.
     *
     * @param rawHeaders the list of raw header strings
     * @return sanitized list of unique header names
     */
    public static List<String> sanitize(List<String> rawHeaders) {
        if (rawHeaders == null || rawHeaders.isEmpty()) {
            return List.of();
        }
        int lastNonEmpty = -1;
        for (int i = 0; i < rawHeaders.size(); i++) {
            String raw = rawHeaders.get(i);
            String val = stripBom(raw);
            if (val != null && !val.trim().isEmpty()) {
                lastNonEmpty = i;
            }
        }
        if (lastNonEmpty == -1) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        Map<String, Integer> seenCounts = new HashMap<>();

        for (int i = 0; i <= lastNonEmpty; i++) {
            String raw = (i < rawHeaders.size()) ? rawHeaders.get(i) : null;
            String val = raw != null ? stripBom(raw).trim() : "";
            if (val.isEmpty()) {
                val = "column_" + (i + 1);
            }
            int count = seenCounts.getOrDefault(val, 0);
            if (count == 0) {
                seenCounts.put(val, 1);
                result.add(val);
            } else {
                seenCounts.put(val, count + 1);
                result.add(val + "_" + count);
            }
        }
        return result;
    }
}
