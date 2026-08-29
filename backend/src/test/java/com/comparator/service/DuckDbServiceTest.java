package com.comparator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DuckDbServiceTest {

    private DuckDbService duckDbService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        duckDbService = new DuckDbService();
    }

    @Test
    @DisplayName("Should convert comma-separated CSV to Parquet and extract headers")
    void shouldConvertCsvToParquetAndGetHeaders() throws IOException {
        Path csvFile = tempDir.resolve("input.csv");
        String content = """
                id,name,amount,active
                1,Alice,100.5,true
                2,Bob,200.0,false
                """;
        Files.writeString(csvFile, content);

        Path parquetFile = tempDir.resolve("output.parquet");

        duckDbService.csvToParquet(csvFile, parquetFile, ',');

        assertThat(Files.exists(parquetFile)).isTrue();
        assertThat(Files.size(parquetFile)).isGreaterThan(0);

        List<String> headers = duckDbService.getColumnHeaders(parquetFile);
        assertThat(headers).containsExactly("id", "name", "amount", "active");
    }

    @Test
    @DisplayName("Should convert tab-separated TXT to Parquet and extract headers")
    void shouldConvertTsvToParquet() throws IOException {
        Path tsvFile = tempDir.resolve("input.tsv");
        String content = "user_id\tusername\trole\n101\tadmin\tADMIN\n102\tuser\tUSER\n";
        Files.writeString(tsvFile, content);

        Path parquetFile = tempDir.resolve("output_tsv.parquet");

        duckDbService.csvToParquet(tsvFile, parquetFile, '\t');

        assertThat(Files.exists(parquetFile)).isTrue();
        List<String> headers = duckDbService.getColumnHeaders(parquetFile);
        assertThat(headers).containsExactly("user_id", "username", "role");
    }

    @Test
    @DisplayName("Should convert pipe-separated TXT to Parquet and extract headers")
    void shouldConvertPsvToParquet() throws IOException {
        Path psvFile = tempDir.resolve("input.psv");
        String content = """
                sku|description|price
                A100|Widget|19.99
                B200|Gadget|29.99
                """;
        Files.writeString(psvFile, content);

        Path parquetFile = tempDir.resolve("output_psv.parquet");

        duckDbService.csvToParquet(psvFile, parquetFile, '|');

        assertThat(Files.exists(parquetFile)).isTrue();
        List<String> headers = duckDbService.getColumnHeaders(parquetFile);
        assertThat(headers).containsExactly("sku", "description", "price");
    }

    @Test
    @DisplayName("Should convert custom delimited file to Parquet and extract headers")
    void shouldConvertCustomDelimitedFileToParquet() throws IOException {
        Path customFile = tempDir.resolve("input.txt");
        String content = """
                code~name~status
                C1~Product1~OK
                C2~Product2~WARN
                """;
        Files.writeString(customFile, content);

        Path parquetFile = tempDir.resolve("output_custom.parquet");
        duckDbService.csvToParquet(customFile, parquetFile, '~');

        assertThat(Files.exists(parquetFile)).isTrue();
        List<String> headers = duckDbService.getColumnHeaders(parquetFile);
        assertThat(headers).containsExactly("code", "name", "status");
    }

    @Test
    @DisplayName("Should convert CSV with quoted comma values like '1,1' without splitting columns")
    void shouldPreserveQuotedCommasInCsvToParquet() throws IOException {
        Path csvFile = tempDir.resolve("quoted_commas.csv");
        String content = """
                id,code,status
                1,"1,1",Active
                2,"2,2",Pending
                """;
        Files.writeString(csvFile, content);

        Path parquetFile = tempDir.resolve("output_quoted.parquet");
        duckDbService.csvToParquet(csvFile, parquetFile, ',');

        assertThat(Files.exists(parquetFile)).isTrue();
        List<String> headers = duckDbService.getColumnHeaders(parquetFile);
        assertThat(headers).containsExactly("id", "code", "status");

        List<java.util.Map<String, Object>> rows = duckDbService.queryParquet(parquetFile, 0, 10);
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).get("code").toString()).isEqualTo("1,1");
        assertThat(rows.get(0).get("status").toString()).isEqualTo("Active");
        assertThat(rows.get(1).get("code").toString()).isEqualTo("2,2");
        assertThat(rows.get(1).get("status").toString()).isEqualTo("Pending");
    }
}
