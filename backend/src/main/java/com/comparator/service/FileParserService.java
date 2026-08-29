package com.comparator.service;

import com.comparator.service.strategy.CsvFileParsingStrategy;
import com.comparator.service.strategy.ExcelFileParsingStrategy;
import com.comparator.service.strategy.FileParsingStrategy;
import com.comparator.service.strategy.TxtFileParsingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final List<FileParsingStrategy> strategies;
    private final FileParsingStrategy defaultStrategy;

    public FileParserService(DelimiterDetector delimiterDetector, DuckDbService duckDbService) {
        this(List.of(
                new CsvFileParsingStrategy(delimiterDetector, duckDbService),
                new TxtFileParsingStrategy(delimiterDetector, duckDbService),
                new ExcelFileParsingStrategy(duckDbService)
        ));
    }

    @Autowired
    public FileParserService(List<FileParsingStrategy> strategies) {
        this.strategies = strategies != null ? strategies : List.of();
        this.defaultStrategy = this.strategies.stream()
                .filter(s -> s instanceof CsvFileParsingStrategy)
                .findFirst()
                .orElse(!this.strategies.isEmpty() ? this.strategies.get(0) : null);
    }

    public List<FileParsingStrategy> getStrategies() {
        return strategies;
    }

    public FileParsingStrategy resolveStrategy(String filename) {
        if (filename != null && !filename.isBlank()) {
            for (FileParsingStrategy strategy : strategies) {
                if (strategy.supports(filename)) {
                    return strategy;
                }
            }
        }
        if (defaultStrategy != null) {
            return defaultStrategy;
        }
        throw new IllegalStateException("No suitable FileParsingStrategy found for filename: " + filename);
    }

    /**
     * Parses a MultipartFile (CSV, TXT, XLS, or XLSX), converts it to Parquet via DuckDB, and returns detected column headers.
     */
    public List<String> parseFileToParquet(MultipartFile file, Path targetParquetPath, String delimiterPreference) throws IOException {
        String filename = file.getOriginalFilename();
        try (InputStream inputStream = file.getInputStream()) {
            return parseStreamToParquet(inputStream, targetParquetPath, delimiterPreference, filename);
        }
    }

    /**
     * Parses an InputStream containing CSV/TXT/Excel data, converts it to Parquet via DuckDB, and returns detected column headers.
     */
    public List<String> parseStreamToParquet(InputStream inputStream, Path targetParquetPath, String delimiterPreference) throws IOException {
        return parseStreamToParquet(inputStream, targetParquetPath, delimiterPreference, null);
    }

    /**
     * Parses an InputStream with optional filename hint, converts it to Parquet via DuckDB, and returns detected column headers.
     */
    public List<String> parseStreamToParquet(InputStream inputStream, Path targetParquetPath, String delimiterPreference, String filename) throws IOException {
        if (targetParquetPath.getParent() != null) {
            Files.createDirectories(targetParquetPath.getParent());
        }

        String suffix = ".tmp";
        if (filename != null && filename.contains(".")) {
            suffix = filename.substring(filename.lastIndexOf('.'));
        }

        Path tempFile = Files.createTempFile("upload-file-", suffix);
        try {
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            FileParsingStrategy strategy = resolveStrategy(filename);
            List<String> headers = strategy.parse(tempFile, targetParquetPath, delimiterPreference, filename);
            if (headers.isEmpty() && !Files.exists(targetParquetPath)) {
                Files.createFile(targetParquetPath);
            }
            return headers;
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
