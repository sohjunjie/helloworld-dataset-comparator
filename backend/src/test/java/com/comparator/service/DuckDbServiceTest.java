package com.comparator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DuckDbServiceTest {

    private DuckDbService duckDbService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        duckDbService = new DuckDbService();
    }

    @Test
    @DisplayName("Should create in-memory DuckDB connection")
    void shouldCreateConnection() throws SQLException {
        try (Connection conn = duckDbService.createConnection()) {
            assertThat(conn).isNotNull();
            assertThat(conn.isClosed()).isFalse();
        }
    }

    @Test
    @DisplayName("Should write rows via ParquetRowWriter, query them, and count records")
    void shouldWriteAndQueryParquet() throws IOException {
        Path parquetFile = tempDir.resolve("output.parquet");
        List<String> headers = List.of("id", "name", "amount");

        try (DuckDbService.ParquetRowWriter writer = duckDbService.createParquetRowWriter(parquetFile, headers)) {
            writer.writeRow(List.of("1", "Alice", "100.50"));
            writer.writeRow(List.of("2", "Bob", "200.00"));
            writer.writeRow(List.of("3", "Charlie", "300.00"));
            writer.finish();
        }

        assertThat(Files.exists(parquetFile)).isTrue();
        assertThat(duckDbService.countParquet(parquetFile)).isEqualTo(3L);

        List<String> detectedHeaders = duckDbService.getColumnHeaders(parquetFile);
        assertThat(detectedHeaders).containsExactly("id", "name", "amount");

        List<Map<String, Object>> rows = duckDbService.queryParquet(parquetFile, 0, 2);
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).get("name")).isEqualTo("Alice");
        assertThat(rows.get(1).get("name")).isEqualTo("Bob");

        List<Map<String, Object>> orderedRows = duckDbService.queryParquet(parquetFile, "id DESC", 0, 10);
        assertThat(orderedRows).hasSize(3);
        assertThat(orderedRows.get(0).get("id")).isEqualTo("3");
    }

    @Test
    @DisplayName("Should execute raw SQL query to maps")
    void shouldExecuteQueryToMaps() {
        List<Map<String, Object>> result = duckDbService.executeQueryToMaps("SELECT 42 AS num, 'hello' AS msg");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("num")).isEqualTo(42);
        assertThat(result.get(0).get("msg")).isEqualTo("hello");
    }

    @Test
    @DisplayName("Should return 0 and empty list when parquet file does not exist")
    void shouldHandleNonExistentParquet() {
        Path nonExistent = tempDir.resolve("non_existent.parquet");
        assertThat(duckDbService.countParquet(nonExistent)).isEqualTo(0L);
        assertThat(duckDbService.queryParquet(nonExistent, 0, 10)).isEmpty();
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when creating writer with empty headers")
    void shouldThrowOnEmptyHeadersForWriter() {
        Path parquetFile = tempDir.resolve("fail.parquet");
        assertThatThrownBy(() -> duckDbService.createParquetRowWriter(parquetFile, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Headers list cannot be empty");
    }
}
