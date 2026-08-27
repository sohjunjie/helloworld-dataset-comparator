package com.comparator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FileParserServiceTest {

    private FileParserService fileParserService;
    private DelimiterDetector delimiterDetector;
    private DuckDbService duckDbService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        delimiterDetector = new DelimiterDetector();
        duckDbService = new DuckDbService();
        fileParserService = new FileParserService(delimiterDetector, duckDbService);
    }

    @Test
    @DisplayName("Should parse CSV with auto-detected delimiter and write Parquet")
    void shouldParseCsvWithAutoDetection() throws IOException {
        String csvContent = "id,name,score\n1,Alice,95.5\n2,Bob,88.0\n";
        MockMultipartFile file = new MockMultipartFile("ds1File", "dataset1.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));
        Path targetParquet = tempDir.resolve("comparison1").resolve("ds1.parquet");

        List<String> headers = fileParserService.parseFileToParquet(file, targetParquet, "auto");

        assertThat(Files.exists(targetParquet)).isTrue();
        assertThat(headers).containsExactly("id", "name", "score");
    }

    @Test
    @DisplayName("Should parse pipe-delimited file with custom delimiter override")
    void shouldParseWithDelimiterOverride() throws IOException {
        String psvContent = "code|desc|qty\nX1|Item1|10\nX2|Item2|20\n";
        MockMultipartFile file = new MockMultipartFile("ds2File", "dataset2.txt", "text/plain", psvContent.getBytes(StandardCharsets.UTF_8));
        Path targetParquet = tempDir.resolve("comparison2").resolve("ds2.parquet");

        List<String> headers = fileParserService.parseFileToParquet(file, targetParquet, "pipe");

        assertThat(Files.exists(targetParquet)).isTrue();
        assertThat(headers).containsExactly("code", "desc", "qty");
    }

    @Test
    @DisplayName("Should parse from raw InputStream")
    void shouldParseFromInputStream() throws IOException {
        String tsvContent = "col1\tcol2\nv1\tv2\n";
        ByteArrayInputStream is = new ByteArrayInputStream(tsvContent.getBytes(StandardCharsets.UTF_8));
        Path targetParquet = tempDir.resolve("comparison3").resolve("ds1.parquet");

        List<String> headers = fileParserService.parseStreamToParquet(is, targetParquet, "\t");

        assertThat(Files.exists(targetParquet)).isTrue();
        assertThat(headers).containsExactly("col1", "col2");
    }
}
