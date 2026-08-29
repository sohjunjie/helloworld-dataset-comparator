package com.comparator.service;

import com.comparator.util.ExcelTestUtils;
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

    @Test
    @DisplayName("Should parse XLSX file via streaming reader and write Parquet")
    void shouldParseXlsxFileToParquet() throws Exception {
        byte[] xlsxBytes = ExcelTestUtils.createTestXlsx(List.of("id", "name", "amount"), List.of(
                List.of("1", "Alice", "100.5"),
                List.of("2", "Bob", "200.0")
        ));
        MockMultipartFile file = new MockMultipartFile("ds1File", "dataset.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxBytes);
        Path targetParquet = tempDir.resolve("excel-test").resolve("ds1.parquet");

        List<String> headers = fileParserService.parseFileToParquet(file, targetParquet, "auto");

        assertThat(Files.exists(targetParquet)).isTrue();
        assertThat(headers).containsExactly("id", "name", "amount");
    }

    @Test
    @DisplayName("Should parse XLS file via HSSFWorkbook and write Parquet")
    void shouldParseXlsFileToParquet() throws Exception {
        byte[] xlsBytes = ExcelTestUtils.createTestXls(List.of("prod_id", "prod_name", "price"), List.of(
                List.of("P1", "Widget", "9.99"),
                List.of("P2", "Gadget", "19.99")
        ));
        MockMultipartFile file = new MockMultipartFile("ds1File", "products.xls", "application/vnd.ms-excel", xlsBytes);
        Path targetParquet = tempDir.resolve("excel-xls-test").resolve("ds1.parquet");

        List<String> headers = fileParserService.parseFileToParquet(file, targetParquet, "auto");

        assertThat(Files.exists(targetParquet)).isTrue();
        assertThat(headers).containsExactly("prod_id", "prod_name", "price");
    }

    @Test
    @DisplayName("Should parse XLSX with only headers and 0 data rows")
    void shouldParseXlsxWithOnlyHeaders() throws Exception {
        byte[] xlsxBytes = ExcelTestUtils.createTestXlsx(List.of("header1", "header2"), List.of());
        MockMultipartFile file = new MockMultipartFile("ds1File", "empty_data.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxBytes);
        Path targetParquet = tempDir.resolve("empty-data-test").resolve("ds1.parquet");

        List<String> headers = fileParserService.parseFileToParquet(file, targetParquet, "auto");

        assertThat(Files.exists(targetParquet)).isTrue();
        assertThat(headers).containsExactly("header1", "header2");
    }

    @Test
    @DisplayName("Should parse XLS with only headers and 0 data rows")
    void shouldParseXlsWithOnlyHeaders() throws Exception {
        byte[] xlsBytes = ExcelTestUtils.createTestXls(List.of("header1", "header2"), List.of());
        MockMultipartFile file = new MockMultipartFile("ds1File", "empty_data.xls", "application/vnd.ms-excel", xlsBytes);
        Path targetParquet = tempDir.resolve("empty-data-xls-test").resolve("ds1.parquet");

        List<String> headers = fileParserService.parseFileToParquet(file, targetParquet, "auto");

        assertThat(Files.exists(targetParquet)).isTrue();
        assertThat(headers).containsExactly("header1", "header2");
    }

    @Test
    @DisplayName("Should parse XLSX with empty sheet")
    void shouldParseXlsxWithEmptySheet() throws Exception {
        byte[] xlsxBytes = ExcelTestUtils.createTestXlsx(List.of(), List.of());
        MockMultipartFile file = new MockMultipartFile("ds1File", "empty_sheet.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxBytes);
        Path targetParquet = tempDir.resolve("empty-sheet-test").resolve("ds1.parquet");

        List<String> headers = fileParserService.parseFileToParquet(file, targetParquet, "auto");

        assertThat(headers).isEmpty();
    }

    @Test
    @DisplayName("Should parse XLS with empty sheet")
    void shouldParseXlsWithEmptySheet() throws Exception {
        byte[] xlsBytes = ExcelTestUtils.createTestXls(List.of(), List.of());
        MockMultipartFile file = new MockMultipartFile("ds1File", "empty_sheet.xls", "application/vnd.ms-excel", xlsBytes);
        Path targetParquet = tempDir.resolve("empty-sheet-xls-test").resolve("ds1.parquet");

        List<String> headers = fileParserService.parseFileToParquet(file, targetParquet, "auto");

        assertThat(headers).isEmpty();
    }

    @Test
    @DisplayName("Should handle mixed data types and sparse cells in XLSX")
    void shouldHandleMixedDataTypesAndSparseCells() throws Exception {
        byte[] xlsxBytes = ExcelTestUtils.createTestXlsx(List.of("colA", "colB", "colC"), List.of(
                List.of("text", "123", "true"),
                List.of("456.78", "", "false")
        ));
        MockMultipartFile file = new MockMultipartFile("ds1File", "mixed.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxBytes);
        Path targetParquet = tempDir.resolve("mixed-test").resolve("ds1.parquet");

        List<String> headers = fileParserService.parseFileToParquet(file, targetParquet, "auto");

        assertThat(Files.exists(targetParquet)).isTrue();
        assertThat(headers).containsExactly("colA", "colB", "colC");
    }

    @Test
    @DisplayName("Should handle mixed data types and sparse cells in XLS")
    void shouldHandleMixedDataTypesAndSparseCellsInXls() throws Exception {
        byte[] xlsBytes = ExcelTestUtils.createTestXls(List.of("colA", "colB", "colC"), List.of(
                List.of("text", "123", "true"),
                List.of("456.78", "", "false")
        ));
        MockMultipartFile file = new MockMultipartFile("ds1File", "mixed.xls", "application/vnd.ms-excel", xlsBytes);
        Path targetParquet = tempDir.resolve("mixed-xls-test").resolve("ds1.parquet");

        List<String> headers = fileParserService.parseFileToParquet(file, targetParquet, "auto");

        assertThat(Files.exists(targetParquet)).isTrue();
        assertThat(headers).containsExactly("colA", "colB", "colC");
    }

    @Test
    @DisplayName("Should parse CSV with quoted commas like '1,1' under auto-detection and preserve columns")
    void shouldParseCsvWithQuotedCommasAndAutoDetection() throws Exception {
        String csvContent = """
                id,code,amount,status
                1,"1,1",100.5,Active
                2,"2,2",200.0,Pending
                """;
        MockMultipartFile file = new MockMultipartFile("ds2File", "dataset2.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));
        Path targetParquet = tempDir.resolve("quoted-csv-test").resolve("ds2.parquet");

        List<String> headers = fileParserService.parseFileToParquet(file, targetParquet, "auto");

        assertThat(Files.exists(targetParquet)).isTrue();
        assertThat(headers).containsExactly("id", "code", "amount", "status");

        List<java.util.Map<String, Object>> rows = duckDbService.queryParquet(targetParquet, 0, 10);
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).get("code").toString()).isEqualTo("1,1");
        assertThat(rows.get(0).get("amount").toString()).isEqualTo("100.5");
        assertThat(rows.get(1).get("code").toString()).isEqualTo("2,2");
    }
}
