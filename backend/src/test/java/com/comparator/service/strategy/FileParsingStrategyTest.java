package com.comparator.service.strategy;

import com.comparator.service.DelimiterDetector;
import com.comparator.service.DuckDbService;
import com.comparator.util.ExcelTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FileParsingStrategyTest {

    private DelimiterDetector delimiterDetector;
    private DuckDbService duckDbService;
    private CsvFileParsingStrategy csvStrategy;
    private TxtFileParsingStrategy txtStrategy;
    private ExcelFileParsingStrategy excelStrategy;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        delimiterDetector = new DelimiterDetector();
        duckDbService = new DuckDbService();
        csvStrategy = new CsvFileParsingStrategy(delimiterDetector, duckDbService);
        txtStrategy = new TxtFileParsingStrategy(delimiterDetector, duckDbService);
        excelStrategy = new ExcelFileParsingStrategy(duckDbService);
    }

    @Test
    @DisplayName("CsvFileParsingStrategy should support only .csv files")
    void csvStrategySupports() {
        assertThat(csvStrategy.supports("data.csv")).isTrue();
        assertThat(csvStrategy.supports("DATA.CSV")).isTrue();
        assertThat(csvStrategy.supports("data.txt")).isFalse();
        assertThat(csvStrategy.supports("data.xlsx")).isFalse();
        assertThat(csvStrategy.supports(null)).isFalse();
    }

    @Test
    @DisplayName("TxtFileParsingStrategy should support text and delimited extensions")
    void txtStrategySupports() {
        assertThat(txtStrategy.supports("data.txt")).isTrue();
        assertThat(txtStrategy.supports("data.tsv")).isTrue();
        assertThat(txtStrategy.supports("data.psv")).isTrue();
        assertThat(txtStrategy.supports("data.tab")).isTrue();
        assertThat(txtStrategy.supports("data.dat")).isTrue();
        assertThat(txtStrategy.supports("data.csv")).isFalse();
        assertThat(txtStrategy.supports("data.xlsx")).isFalse();
        assertThat(txtStrategy.supports(null)).isFalse();
    }

    @Test
    @DisplayName("ExcelFileParsingStrategy should support .xlsx and .xls files")
    void excelStrategySupports() {
        assertThat(excelStrategy.supports("data.xlsx")).isTrue();
        assertThat(excelStrategy.supports("data.xls")).isTrue();
        assertThat(excelStrategy.supports("DATA.XLSX")).isTrue();
        assertThat(excelStrategy.supports("data.csv")).isFalse();
        assertThat(excelStrategy.supports("data.txt")).isFalse();
        assertThat(excelStrategy.supports(null)).isFalse();
    }

    @Test
    @DisplayName("CsvFileParsingStrategy parses complex CSV with quotes and commas using Commons CSV")
    void csvStrategyParsesComplexCsv() throws IOException {
        String csvContent = "id,name,description,amount\n" +
                "1,\"Smith, John\",\"Item with \"\"quotes\"\" and comma, inside\",100.50\n" +
                "2,\"Bob\",\"Simple line\",200.00\n";
        Path csvFile = tempDir.resolve("test.csv");
        Files.writeString(csvFile, csvContent, StandardCharsets.UTF_8);

        Path targetParquet = tempDir.resolve("csv_output.parquet");
        List<String> headers = csvStrategy.parse(csvFile, targetParquet, "auto", "test.csv");

        assertThat(headers).containsExactly("id", "name", "description", "amount");
        assertThat(Files.exists(targetParquet)).isTrue();

        List<Map<String, Object>> rows = duckDbService.queryParquet(targetParquet, 0, 10);
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).get("name")).isEqualTo("Smith, John");
        assertThat(rows.get(0).get("description")).isEqualTo("Item with \"quotes\" and comma, inside");
    }

    @Test
    @DisplayName("TxtFileParsingStrategy parses pipe-delimited data accurately")
    void txtStrategyParsesPipeDelimited() throws IOException {
        String txtContent = "col1|col2|col3\nval1|val2|val3\n";
        Path txtFile = tempDir.resolve("test.txt");
        Files.writeString(txtFile, txtContent, StandardCharsets.UTF_8);

        Path targetParquet = tempDir.resolve("txt_output.parquet");
        List<String> headers = txtStrategy.parse(txtFile, targetParquet, "pipe", "test.txt");

        assertThat(headers).containsExactly("col1", "col2", "col3");
        assertThat(Files.exists(targetParquet)).isTrue();

        List<Map<String, Object>> rows = duckDbService.queryParquet(targetParquet, 0, 10);
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("col1")).isEqualTo("val1");
        assertThat(rows.get(0).get("col2")).isEqualTo("val2");
        assertThat(rows.get(0).get("col3")).isEqualTo("val3");
    }

    @Test
    @DisplayName("ExcelFileParsingStrategy parses XLSX and XLS files accurately")
    void excelStrategyParsesWorkbooks() throws Exception {
        byte[] xlsxBytes = ExcelTestUtils.createTestXlsx(List.of("id", "val"), List.of(
                List.of("1", "A"),
                List.of("2", "B")
        ));
        Path xlsxFile = tempDir.resolve("test.xlsx");
        Files.write(xlsxFile, xlsxBytes);

        Path targetParquet = tempDir.resolve("excel_output.parquet");
        List<String> headers = excelStrategy.parse(xlsxFile, targetParquet, "auto", "test.xlsx");

        assertThat(headers).containsExactly("id", "val");
        assertThat(Files.exists(targetParquet)).isTrue();
    }

    @Test
    @DisplayName("ExcelFileParsingStrategy strips BOM from headers and cell values in XLSX")
    void excelStrategyStripsBomFromXlsx() throws Exception {
        byte[] xlsxBytes = ExcelTestUtils.createTestXlsx(
                List.of("\uFEFFid", "\uFEFFname", "amount"),
                List.of(
                        List.of("\uFEFF1", "\uFEFFAlice", "100"),
                        List.of("2", "Bob", "\uFEFF200")
                )
        );
        Path xlsxFile = tempDir.resolve("bom_test.xlsx");
        Files.write(xlsxFile, xlsxBytes);

        Path targetParquet = tempDir.resolve("bom_excel_output.parquet");
        List<String> headers = excelStrategy.parse(xlsxFile, targetParquet, "auto", "bom_test.xlsx");

        assertThat(headers).containsExactly("id", "name", "amount");
        assertThat(Files.exists(targetParquet)).isTrue();

        List<Map<String, Object>> rows = duckDbService.queryParquet(targetParquet, 0, 10);
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).get("id")).isEqualTo("1");
        assertThat(rows.get(0).get("name")).isEqualTo("Alice");
        assertThat(rows.get(0).get("amount")).isEqualTo("100");
        assertThat(rows.get(1).get("id")).isEqualTo("2");
        assertThat(rows.get(1).get("name")).isEqualTo("Bob");
        assertThat(rows.get(1).get("amount")).isEqualTo("200");
    }

    @Test
    @DisplayName("ExcelFileParsingStrategy strips BOM from headers and cell values in XLS")
    void excelStrategyStripsBomFromXls() throws Exception {
        byte[] xlsBytes = ExcelTestUtils.createTestXls(
                List.of("\uFEFFid", "\uFEFFdescription"),
                List.of(
                        List.of("\uFEFF10", "\uFEFFTest item"),
                        List.of("20", "Second item")
                )
        );
        Path xlsFile = tempDir.resolve("bom_test.xls");
        Files.write(xlsFile, xlsBytes);

        Path targetParquet = tempDir.resolve("bom_xls_output.parquet");
        List<String> headers = excelStrategy.parse(xlsFile, targetParquet, "auto", "bom_test.xls");

        assertThat(headers).containsExactly("id", "description");
        assertThat(Files.exists(targetParquet)).isTrue();

        List<Map<String, Object>> rows = duckDbService.queryParquet(targetParquet, 0, 10);
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).get("id")).isEqualTo("10");
        assertThat(rows.get(0).get("description")).isEqualTo("Test item");
        assertThat(rows.get(1).get("id")).isEqualTo("20");
        assertThat(rows.get(1).get("description")).isEqualTo("Second item");
    }

    @Test
    @DisplayName("CsvFileParsingStrategy strips BOM from headers and row cells")
    void csvStrategyStripsBom() throws IOException {
        String csvContent = "\uFEFFid,\uFEFFname,score\n\uFEFF101,\uFEFFCharlie,95\n";
        Path csvFile = tempDir.resolve("bom_test.csv");
        Files.writeString(csvFile, csvContent, StandardCharsets.UTF_8);

        Path targetParquet = tempDir.resolve("bom_csv_output.parquet");
        List<String> headers = csvStrategy.parse(csvFile, targetParquet, "auto", "bom_test.csv");

        assertThat(headers).containsExactly("id", "name", "score");
        assertThat(Files.exists(targetParquet)).isTrue();

        List<Map<String, Object>> rows = duckDbService.queryParquet(targetParquet, 0, 10);
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("id")).isEqualTo("101");
        assertThat(rows.get(0).get("name")).isEqualTo("Charlie");
        assertThat(rows.get(0).get("score")).isEqualTo("95");
    }
}
