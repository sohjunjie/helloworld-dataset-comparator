package com.comparator.service.strategy;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FileParsingStrategy {

    /**
     * Returns true if this strategy supports parsing the given filename.
     *
     * @param filename name or path of the file, may be null
     * @return true if supported, false otherwise
     */
    boolean supports(String filename);

    /**
     * Parses the given file and writes its contents to the target Parquet path.
     *
     * @param sourceFile path to the input file
     * @param targetParquetPath path where the Parquet file should be written
     * @param delimiterPreference preference for delimiter detection/override (e.g. "auto", "comma", "tab", "pipe", etc.)
     * @return detected and sanitized column headers
     * @throws IOException if parsing or writing fails
     */
    default List<String> parse(Path sourceFile, Path targetParquetPath, String delimiterPreference) throws IOException {
        return parse(sourceFile, targetParquetPath, delimiterPreference, null);
    }

    /**
     * Parses the given file with an optional original filename hint and writes its contents to the target Parquet path.
     *
     * @param sourceFile path to the input file
     * @param targetParquetPath path where the Parquet file should be written
     * @param delimiterPreference preference for delimiter detection/override (e.g. "auto", "comma", "tab", "pipe", etc.)
     * @param originalFilename original filename before temp file creation, may be null
     * @return detected and sanitized column headers
     * @throws IOException if parsing or writing fails
     */
    List<String> parse(Path sourceFile, Path targetParquetPath, String delimiterPreference, String originalFilename) throws IOException;
}
