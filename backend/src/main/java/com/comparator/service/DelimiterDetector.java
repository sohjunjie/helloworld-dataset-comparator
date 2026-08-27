package com.comparator.service;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DelimiterDetector {

    public static final char[] CANDIDATES = {',', '\t', '|', ';'};
    public static final char DEFAULT_DELIMITER = ',';

    private static final Map<String, Character> KNOWN_DELIMITERS = new HashMap<>();

    static {
        KNOWN_DELIMITERS.put("comma", ',');
        KNOWN_DELIMITERS.put(",", ',');
        KNOWN_DELIMITERS.put("tab", '\t');
        KNOWN_DELIMITERS.put("\t", '\t');
        KNOWN_DELIMITERS.put("\\t", '\t');
        KNOWN_DELIMITERS.put("pipe", '|');
        KNOWN_DELIMITERS.put("|", '|');
        KNOWN_DELIMITERS.put("semicolon", ';');
        KNOWN_DELIMITERS.put(";", ';');
    }

    /**
     * Resolves the delimiter preference. If override is specified ("auto", "comma", "tab", "pipe", "semicolon",
     * or a custom single character), uses the override; otherwise auto-detects from the input sample.
     */
    public char resolveDelimiter(String delimiterPreference, InputStream sample) {
        if (delimiterPreference == null || delimiterPreference.isBlank() || "auto".equalsIgnoreCase(delimiterPreference.trim())) {
            return detect(sample);
        }

        String normalized = delimiterPreference.trim().toLowerCase();
        Character known = KNOWN_DELIMITERS.get(normalized);
        if (known != null) {
            return known;
        }

        // Direct check on original string for case-sensitive custom single character
        String originalTrimmed = delimiterPreference.trim();
        if (originalTrimmed.length() == 1) {
            return originalTrimmed.charAt(0);
        }

        throw new IllegalArgumentException("Invalid delimiter preference: " + delimiterPreference +
                ". Must be 'auto', 'comma', 'tab', 'pipe', 'semicolon', or a single character.");
    }

    /**
     * Read the first 10 lines of the file, count occurrences of each candidate delimiter,
     * pick the consistent delimiter with the highest count. If ambiguous or none consistent, default to comma.
     */
    public char detect(InputStream sample) {
        if (sample == null) {
            return DEFAULT_DELIMITER;
        }

        List<String> sampleLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(sample, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null && sampleLines.size() < 10) {
                if (!line.trim().isEmpty()) {
                    sampleLines.add(line);
                }
            }
        } catch (IOException e) {
            return DEFAULT_DELIMITER;
        }

        if (sampleLines.isEmpty()) {
            return DEFAULT_DELIMITER;
        }

        char bestCandidate = DEFAULT_DELIMITER;
        int bestCount = 0;
        int candidatesWithBestCount = 0;

        for (char candidate : CANDIDATES) {
            int firstCount = countOccurrences(sampleLines.get(0), candidate);
            if (firstCount <= 0) {
                continue;
            }

            boolean consistent = true;
            for (int i = 1; i < sampleLines.size(); i++) {
                if (countOccurrences(sampleLines.get(i), candidate) != firstCount) {
                    consistent = false;
                    break;
                }
            }

            if (consistent) {
                if (firstCount > bestCount) {
                    bestCandidate = candidate;
                    bestCount = firstCount;
                    candidatesWithBestCount = 1;
                } else if (firstCount == bestCount) {
                    candidatesWithBestCount++;
                }
            }
        }

        // If there's a tie between multiple candidates with the highest count, it's ambiguous -> default to comma
        if (candidatesWithBestCount > 1) {
            return DEFAULT_DELIMITER;
        }

        return bestCount > 0 ? bestCandidate : DEFAULT_DELIMITER;
    }

    private int countOccurrences(String line, char target) {
        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == target) {
                count++;
            }
        }
        return count;
    }
}
