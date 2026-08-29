package com.comparator.service.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HeaderSanitizer {

    private HeaderSanitizer() {
    }

    /**
     * Sanitizes raw header names: trims whitespace, fills empty header names with column_<index>,
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
            String val = rawHeaders.get(i);
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
            String val = (i < rawHeaders.size() && rawHeaders.get(i) != null) ? rawHeaders.get(i).trim() : "";
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
