package com.comparator.service.strategy;

import com.comparator.service.DelimiterDetector;
import com.comparator.service.DuckDbService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
public class TxtFileParsingStrategy extends AbstractDelimitedFileParsingStrategy {

    public TxtFileParsingStrategy(DelimiterDetector delimiterDetector, DuckDbService duckDbService) {
        super(delimiterDetector, duckDbService);
    }

    @Override
    public boolean supports(String filename) {
        if (filename == null) {
            return false;
        }
        String lower = filename.toLowerCase();
        return lower.endsWith(".txt") || lower.endsWith(".tsv") || lower.endsWith(".psv") || lower.endsWith(".tab") || lower.endsWith(".dat");
    }
}
