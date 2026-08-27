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

    /**
     * Interface for streaming rows into a DuckDB Parquet file.
     */
    public interface ParquetRowWriter extends AutoCloseable {
        void writeRow(List<String> row);
        void finish();
        @Override
        void close();
    }

    /**
     * Creates a streaming ParquetRowWriter backed by an in-memory DuckDB table.
     */
    public ParquetRowWriter createParquetRowWriter(Path parquetPath, List<String> headers) {
        if (headers == null || headers.isEmpty()) {
            throw new IllegalArgumentException("Headers list cannot be empty when creating Parquet writer");
        }

        String normalizedParquetPath = normalizePath(parquetPath);
        String tableName = "t_" + java.util.UUID.randomUUID().toString().replace("-", "");

        StringBuilder createSql = new StringBuilder("CREATE TEMPORARY TABLE ").append(tableName).append(" (");
        StringBuilder insertSql = new StringBuilder("INSERT INTO ").append(tableName).append(" VALUES (");
        for (int i = 0; i < headers.size(); i++) {
            if (i > 0) {
                createSql.append(", ");
                insertSql.append(", ");
            }
            createSql.append("\"").append(headers.get(i).replace("\"", "\"\"")).append("\" VARCHAR");
            insertSql.append("?");
        }
        createSql.append(")");
        insertSql.append(")");

        String copySql = String.format("COPY %s TO '%s' (FORMAT PARQUET)", tableName, normalizedParquetPath);

        try {
            Connection conn = createConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createSql.toString());
            }

            java.sql.PreparedStatement pstmt = conn.prepareStatement(insertSql.toString());

            return new ParquetRowWriter() {
                private int batchCount = 0;
                private boolean finished = false;

                @Override
                public void writeRow(List<String> row) {
                    try {
                        for (int i = 0; i < headers.size(); i++) {
                            String val = (row != null && i < row.size()) ? row.get(i) : null;
                            pstmt.setString(i + 1, val);
                        }
                        pstmt.addBatch();
                        batchCount++;
                        if (batchCount % 1000 == 0) {
                            pstmt.executeBatch();
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException("Failed to add batch row: " + e.getMessage(), e);
                    }
                }

                @Override
                public void finish() {
                    if (finished) {
                        return;
                    }
                    try {
                        if (batchCount % 1000 != 0 || batchCount == 0) {
                            pstmt.executeBatch();
                        }
                        try (Statement stmt = conn.createStatement()) {
                            stmt.execute(copySql);
                        }
                        finished = true;
                    } catch (SQLException e) {
                        throw new RuntimeException("Failed to export table to Parquet: " + e.getMessage(), e);
                    }
                }

                @Override
                public void close() {
                    try {
                        if (!finished) {
                            finish();
                        }
                    } finally {
                        try {
                            pstmt.close();
                        } catch (Exception ignored) {
                        }
                        try {
                            conn.close();
                        } catch (Exception ignored) {
                        }
                    }
                }
            };
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize DuckDB Parquet writer: " + e.getMessage(), e);
        }
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
