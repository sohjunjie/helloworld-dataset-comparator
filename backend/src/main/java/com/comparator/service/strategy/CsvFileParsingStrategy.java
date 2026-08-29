package com.comparator.service.strategy;

import com.comparator.service.DelimiterDetector;
import com.comparator.service.DuckDbService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class CsvFileParsingStrategy extends AbstractDelimitedFileParsingStrategy {

    public CsvFileParsingStrategy(DelimiterDetector delimiterDetector, DuckDbService duckDbService) {
        super(delimiterDetector, duckDbService);
    }

    @Override
    public boolean supports(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".csv");
    }
}
