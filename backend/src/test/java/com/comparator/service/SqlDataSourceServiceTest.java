package com.comparator.service;

import com.comparator.model.dto.DatabaseConnectionConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SqlDataSourceServiceTest {

    private static final String H2_URL = "jdbc:h2:mem:sql_ds_test;DB_CLOSE_DELAY=-1";
    private DuckDbService duckDbService;
    private SqlDataSourceService sqlDataSourceService;

    @BeforeEach
    void setUp() throws SQLException {
        duckDbService = new DuckDbService();
        sqlDataSourceService = new SqlDataSourceService(duckDbService, config -> DriverManager.getConnection(H2_URL, "sa", ""));

        try (Connection conn = DriverManager.getConnection(H2_URL, "sa", "");
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (user_id INT, user_name VARCHAR, role VARCHAR);");
            stmt.execute("DELETE FROM users;");
            stmt.execute("INSERT INTO users VALUES (1, 'Alice', 'Admin'), (2, 'Bob', 'User');");
        }
    }

    @Nested
    @DisplayName("SQL Validation Tests")
    class SqlValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "SELECT * FROM users",
                "select id, name, email from customers where active = true",
                "WITH active_users AS (SELECT * FROM users WHERE active = true) SELECT * FROM active_users",
                "/* Leading block comment */ SELECT * FROM orders",
                "-- Leading line comment\nSELECT id, total FROM transactions",
                "SELECT id, 'hello world' AS greeting, is_deleted, create_time FROM products",
                "SELECT id, name FROM users WHERE remarks = 'DROP TABLE users'"
        })
        @DisplayName("Should accept valid SELECT and CTE statements")
        void shouldAcceptValidSelectQueries(String sql) {
            sqlDataSourceService.validateSelectSql(sql);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "",
                "   ",
                "-- only comment\n",
                "/* only block comment */"
        })
        @DisplayName("Should reject empty or whitespace-only queries with 400 Bad Request")
        void shouldRejectEmptyQueries(String sql) {
            assertThatThrownBy(() -> sqlDataSourceService.validateSelectSql(sql))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "INSERT INTO users (name) VALUES ('Hacker')",
                "UPDATE users SET name = 'admin'",
                "DELETE FROM users WHERE id = 1",
                "DROP TABLE users",
                "CREATE TABLE foo (id INT)",
                "ALTER TABLE users ADD COLUMN is_admin BOOLEAN",
                "TRUNCATE TABLE logs",
                "GRANT ALL ON users TO public",
                "REVOKE ALL ON users FROM public",
                "EXEC sp_executesql 'SELECT 1'",
                "SELECT * FROM users; DROP TABLE users;",
                "SELECT * FROM users; DELETE FROM orders"
        })
        @DisplayName("Should reject non-SELECT and DDL/DML queries with 400 Bad Request")
        void shouldRejectDmlAndDdlQueries(String sql) {
            assertThatThrownBy(() -> sqlDataSourceService.validateSelectSql(sql))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        }
    }

    @Nested
    @DisplayName("Query Execution & Parquet Conversion Tests")
    class ExecutionTests {

        @Test
        @DisplayName("Should execute SELECT query and stream results into Parquet file")
        void shouldExecuteAndConvertToParquet(@TempDir Path tempDir) {
            DatabaseConnectionConfig config = new DatabaseConnectionConfig(
                    "localhost", 5432, "testdb", "postgres", "secret");

            Path targetParquet = tempDir.resolve("sql_ds.parquet");
            List<String> headers = sqlDataSourceService.executeAndConvertToParquet(
                    config, "SELECT user_id, user_name, role FROM users", targetParquet);

            assertThat(headers).containsExactly("USER_ID", "USER_NAME", "ROLE");
            assertThat(Files.exists(targetParquet)).isTrue();
            assertThat(duckDbService.getColumnHeaders(targetParquet)).containsExactly("USER_ID", "USER_NAME", "ROLE");
        }

        @Test
        @DisplayName("Should handle database connection failure with 400 Bad Request")
        void shouldHandleConnectionFailure(@TempDir Path tempDir) {
            DatabaseConnectionConfig config = new DatabaseConnectionConfig(
                    "unreachable.host", 5432, "testdb", "postgres", "secret");

            SqlDataSourceService errorService = new SqlDataSourceService(duckDbService, cfg -> {
                throw new SQLException("Connection refused: connect", "08001");
            });

            Path targetParquet = tempDir.resolve("fail.parquet");
            assertThatThrownBy(() -> errorService.executeAndConvertToParquet(
                    config, "SELECT * FROM users", targetParquet))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("Should handle query timeout with 408 Request Timeout")
        void shouldHandleQueryTimeout(@TempDir Path tempDir) {
            DatabaseConnectionConfig config = new DatabaseConnectionConfig(
                    "localhost", 5432, "testdb", "postgres", "secret");

            Connection mockConn = (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    (proxy, method, args) -> {
                        if ("createStatement".equals(method.getName())) {
                            return Proxy.newProxyInstance(
                                    Statement.class.getClassLoader(),
                                    new Class<?>[]{Statement.class},
                                    (sProxy, sMethod, sArgs) -> {
                                        if ("executeQuery".equals(sMethod.getName())) {
                                            throw new SQLTimeoutException("Statement cancelled due to timeout");
                                        }
                                        return null;
                                    }
                            );
                        }
                        return null;
                    }
            );

            SqlDataSourceService timeoutService = new SqlDataSourceService(duckDbService, cfg -> mockConn);

            Path targetParquet = tempDir.resolve("timeout.parquet");
            assertThatThrownBy(() -> timeoutService.executeAndConvertToParquet(
                    config, "SELECT * FROM users", targetParquet))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.REQUEST_TIMEOUT));
        }

        @Test
        @DisplayName("Should handle SQL syntax error with 400 Bad Request")
        void shouldHandleSqlSyntaxError(@TempDir Path tempDir) {
            DatabaseConnectionConfig config = new DatabaseConnectionConfig(
                    "localhost", 5432, "testdb", "postgres", "secret");

            Path targetParquet = tempDir.resolve("syntax_error.parquet");
            assertThatThrownBy(() -> sqlDataSourceService.executeAndConvertToParquet(
                    config, "SELECT * FROM non_existing_table_xyz", targetParquet))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        }
    }
}
