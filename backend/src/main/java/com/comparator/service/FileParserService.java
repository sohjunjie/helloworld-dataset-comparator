package com.comparator.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class FileParserService {

    private final DelimiterDetector delimiterDetector;
    private final DuckDbService duckDbService;

    public FileParserService(DelimiterDetector delimiterDetector, DuckDbService duckDbService) {
        this.delimiterDetector = delimiterDetector;
        this.duckDbService = duckDbService;
    }

    /**
     * Parses a MultipartFile (CSV or TXT), converts it to Parquet via DuckDB, and returns detected column headers.
     */
    public List<String> parseFileToParquet(MultipartFile file, Path targetParquetPath, String delimiterPreference) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return parseStreamToParquet(inputStream, targetParquetPath, delimiterPreference);
        }
    }

    /**
     * Parses an InputStream containing CSV/TXT data, converts it to Parquet via DuckDB, and returns detected column headers.
     */
    public List<String> parseStreamToParquet(InputStream inputStream, Path targetParquetPath, String delimiterPreference) throws IOException {
        if (targetParquetPath.getParent() != null) {
            Files.createDirectories(targetParquetPath.getParent());
        }

        Path tempFile = Files.createTempFile("upload-csv-", ".tmp");
        try {
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            char delimiter;
            try (InputStream sampleStream = Files.newInputStream(tempFile)) {
                delimiter = delimiterDetector.resolveDelimiter(delimiterPreference, sampleStream);
            }

            duckDbService.csvToParquet(tempFile, targetParquetPath, delimiter);
            return duckDbService.getColumnHeaders(targetParquetPath);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
