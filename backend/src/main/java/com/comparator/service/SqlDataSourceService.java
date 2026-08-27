package com.comparator.service;

import com.comparator.model.dto.DatabaseConnectionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class SqlDataSourceService {

    private static final Logger log = LoggerFactory.getLogger(SqlDataSourceService.class);

    private static final String[] PROHIBITED_KEYWORDS = {
            "INSERT", "UPDATE", "DELETE", "DROP", "CREATE", "ALTER",
            "TRUNCATE", "GRANT", "REVOKE", "EXEC", "EXECUTE", "CALL"
    };

    private final DuckDbService duckDbService;
    private final JdbcConnectionProvider connectionProvider;

    public SqlDataSourceService(DuckDbService duckDbService, JdbcConnectionProvider connectionProvider) {
        this.duckDbService = duckDbService;
        this.connectionProvider = connectionProvider;
    }

    /**
     * Validates that the provided SQL query is a SELECT statement and rejects any DDL/DML commands.
     */
    public void validateSelectSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SQL query cannot be empty");
        }

        String cleaned = stripComments(sql).trim();
        if (cleaned.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SQL query cannot be empty");
        }

        String withoutLiterals = removeStringLiterals(cleaned).trim();
        String firstWord = getFirstWord(withoutLiterals);
        if (!firstWord.equalsIgnoreCase("SELECT") && !firstWord.equalsIgnoreCase("WITH")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only SELECT queries are permitted. Query must begin with SELECT or WITH, but found: " + firstWord);
        }

        String[] statements = withoutLiterals.split(";");
        for (int i = 0; i < statements.length; i++) {
            String stmt = statements[i].trim();
            if (stmt.isEmpty()) {
                continue;
            }
            String first = getFirstWord(stmt);
            if (!first.equalsIgnoreCase("SELECT") && !first.equalsIgnoreCase("WITH")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Only SELECT statements are permitted. Statement " + (i + 1) + " started with: " + first);
            }
        }

        for (String keyword : PROHIBITED_KEYWORDS) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b", Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(withoutLiterals).find()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Only SELECT queries are permitted. Prohibited keyword detected: " + keyword);
            }
        }
    }

    /**
     * Executes the SQL SELECT query against PostgreSQL and streams the results into Parquet.
     * Returns the detected column headers.
     */
    public List<String> executeAndConvertToParquet(DatabaseConnectionConfig config, String sql, Path targetParquetPath) {
        if (config == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Database connection details are required");
        }
        validateSelectSql(sql);

        try {
            if (targetParquetPath.getParent() != null) {
                Files.createDirectories(targetParquetPath.getParent());
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create storage directory", e);
        }

        try (Connection conn = connectionProvider.getConnection(config)) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                stmt.setFetchSize(1000);
                stmt.setQueryTimeout(30);

                try (ResultSet rs = stmt.executeQuery(sql)) {
                    return duckDbService.resultSetToParquet(rs, targetParquetPath);
                }
            } finally {
                try {
                    conn.rollback();
                } catch (Exception ignored) {
                }
            }
        } catch (SQLTimeoutException e) {
            log.warn("SQL query execution timed out: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Query execution timed out: " + e.getMessage(), e);
        } catch (SQLException e) {
            log.error("Database or SQL error: SQLState={}, ErrorCode={}, Message={}", e.getSQLState(), e.getErrorCode(), e.getMessage());
            String message = e.getMessage() != null ? e.getMessage() : "Database error";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Database query error: " + message, e);
        } catch (Exception e) {
            log.error("Unexpected error during SQL execution and Parquet conversion", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to execute database query: " + e.getMessage(), e);
        }
    }

    private static String stripComments(String sql) {
        StringBuilder sb = new StringBuilder();
        int len = sql.length();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int i = 0;
        while (i < len) {
            char c = sql.charAt(i);
            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
                sb.append(c);
                i++;
            } else if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                sb.append(c);
                i++;
            } else if (!inSingleQuote && !inDoubleQuote && c == '-' && i + 1 < len && sql.charAt(i + 1) == '-') {
                i += 2;
                while (i < len && sql.charAt(i) != '\n' && sql.charAt(i) != '\r') {
                    i++;
                }
            } else if (!inSingleQuote && !inDoubleQuote && c == '/' && i + 1 < len && sql.charAt(i + 1) == '*') {
                i += 2;
                while (i + 1 < len && !(sql.charAt(i) == '*' && sql.charAt(i + 1) == '/')) {
                    i++;
                }
                i += 2;
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    private static String removeStringLiterals(String sql) {
        StringBuilder sb = new StringBuilder();
        int len = sql.length();
        boolean inSingleQuote = false;
        int i = 0;
        while (i < len) {
            char c = sql.charAt(i);
            if (c == '\'') {
                if (inSingleQuote && i + 1 < len && sql.charAt(i + 1) == '\'') {
                    i += 2;
                    continue;
                }
                inSingleQuote = !inSingleQuote;
                sb.append(' ');
                i++;
            } else if (inSingleQuote) {
                sb.append(' ');
                i++;
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    private static String getFirstWord(String sql) {
        String trimmed = sql.trim();
        int spaceIdx = -1;
        for (int i = 0; i < trimmed.length(); i++) {
            if (Character.isWhitespace(trimmed.charAt(i)) || trimmed.charAt(i) == '(') {
                spaceIdx = i;
                break;
            }
        }
        return spaceIdx == -1 ? trimmed : trimmed.substring(0, spaceIdx);
    }
}
