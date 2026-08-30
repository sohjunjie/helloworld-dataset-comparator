package com.comparator.service.strategy;

import com.comparator.service.DelimiterDetector;
import com.comparator.service.DuckDbService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractDelimitedFileParsingStrategy implements FileParsingStrategy {

    protected final DelimiterDetector delimiterDetector;
    protected final DuckDbService duckDbService;

    public AbstractDelimitedFileParsingStrategy(DelimiterDetector delimiterDetector, DuckDbService duckDbService) {
        this.delimiterDetector = delimiterDetector;
        this.duckDbService = duckDbService;
    }

    @Override
    public List<String> parse(Path sourceFile, Path targetParquetPath, String delimiterPreference, String originalFilename) throws IOException {
        char delimiter;
        try (InputStream sampleStream = Files.newInputStream(sourceFile)) {
            delimiter = delimiterDetector.resolveDelimiter(delimiterPreference, sampleStream);
        }

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setDelimiter(delimiter)
                .setIgnoreSurroundingSpaces(false)
                .setAllowMissingColumnNames(true)
                .setQuote('"')
                .setNullString("")
                .build();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(sourceFile), StandardCharsets.UTF_8));
             CSVParser parser = csvFormat.parse(reader)) {

            Iterator<CSVRecord> iterator = parser.iterator();
            if (!iterator.hasNext()) {
                return List.of();
            }

            CSVRecord headerRecord = iterator.next();
            List<String> rawHeaders = new ArrayList<>(headerRecord.size());
            for (int i = 0; i < headerRecord.size(); i++) {
                String h = headerRecord.get(i);
                rawHeaders.add(h != null ? h : "");
            }

            List<String> headers = HeaderSanitizer.sanitize(rawHeaders);
            if (headers.isEmpty()) {
                return List.of();
            }

            try (DuckDbService.ParquetRowWriter writer = duckDbService.createParquetRowWriter(targetParquetPath, headers)) {
                while (iterator.hasNext()) {
                    CSVRecord record = iterator.next();
                    List<String> row = new ArrayList<>(headers.size());
                    for (int i = 0; i < headers.size(); i++) {
                        String val = (i < record.size()) ? record.get(i) : null;
                        row.add(HeaderSanitizer.stripBom(val));
                    }
                    writer.writeRow(row);
                }
                writer.finish();
            }

            return headers;
        }
    }
}
