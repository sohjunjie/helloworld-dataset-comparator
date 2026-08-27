package com.comparator.service;

import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Service
public class DuckDbService {

    private static final String DUCKDB_IN_MEMORY_URL = "jdbc:duckdb:";

    /**
     * Create an in-memory DuckDB connection.
     */
    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection(DUCKDB_IN_MEMORY_URL);
    }

    /**
     * Convert CSV/TXT file to Parquet format using DuckDB.
     */
    public void csvToParquet(Path csvPath, Path parquetPath, char delimiter) {
        String normalizedCsvPath = normalizePath(csvPath);
        String normalizedParquetPath = normalizePath(parquetPath);
        String delimEscaped = formatDelimiter(delimiter);

        String sql = String.format(
                "COPY (SELECT * FROM read_csv('%s', delim='%s', header=true, auto_detect=true)) TO '%s' (FORMAT PARQUET)",
                normalizedCsvPath,
                delimEscaped,
                normalizedParquetPath
        );

        try (Connection conn = createConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to convert CSV to Parquet: " + e.getMessage(), e);
        }
    }

    /**
     * Read Parquet schema and return list of column names.
     */
    public List<String> getColumnHeaders(Path parquetPath) {
        String normalizedParquetPath = normalizePath(parquetPath);
        String sql = String.format("SELECT * FROM read_parquet('%s') LIMIT 0", normalizedParquetPath);

        List<String> headers = new ArrayList<>();
        try (Connection conn = createConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                headers.add(meta.getColumnName(i));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to read Parquet headers: " + e.getMessage(), e);
        }
        return headers;
    }

    private String normalizePath(Path path) {
        return path.toAbsolutePath().toString().replace('\\', '/').replace("'", "''");
    }

    private String formatDelimiter(char delimiter) {
        if (delimiter == '\t') {
            return "\\t";
        }
        if (delimiter == '\'') {
            return "''";
        }
        if (delimiter == '\\') {
            return "\\\\";
        }
        return String.valueOf(delimiter);
    }
}
