package com.comparator.controller;

import com.comparator.config.AppProperties;
import com.comparator.model.dto.DatabaseConnectionConfig;
import com.comparator.model.dto.UploadConfigRequest;
import com.comparator.model.entity.ComparisonRecord;
import com.comparator.model.enums.ComparisonStatus;
import com.comparator.model.enums.DataSourceType;
import com.comparator.repository.ComparisonRepository;
import com.comparator.service.JdbcConnectionProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ComparisonSqlUploadControllerTest.TestJdbcConfig.class)
class ComparisonSqlUploadControllerTest {

    private static final String H2_URL = "jdbc:h2:mem:mock_sql_upload;DB_CLOSE_DELAY=-1";

    @TestConfiguration
    static class TestJdbcConfig {
        @Bean
        @Primary
        public JdbcConnectionProvider testJdbcConnectionProvider() {
            return config -> {
                if ("unreachable".equals(config.host())) {
                    throw new SQLException("Connection refused");
                }
                if ("timeout_host".equals(config.host())) {
                    return (Connection) Proxy.newProxyInstance(
                            Connection.class.getClassLoader(),
                            new Class<?>[]{Connection.class},
                            (p, m, a) -> {
                                if ("createStatement".equals(m.getName())) {
                                    return Proxy.newProxyInstance(
                                            Statement.class.getClassLoader(),
                                            new Class<?>[]{Statement.class},
                                            (sp, sm, sa) -> {
                                                if ("executeQuery".equals(sm.getName())) {
                                                    throw new SQLTimeoutException("Query timed out");
                                                }
                                                return null;
                                            }
                                    );
                                }
                                return null;
                            }
                    );
                }
                return DriverManager.getConnection(H2_URL, "sa", "");
            };
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ComparisonRepository comparisonRepository;

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setupMockDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(H2_URL, "sa", "");
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS staff (id INT, name VARCHAR, salary INT);");
            stmt.execute("DELETE FROM staff;");
            stmt.execute("INSERT INTO staff VALUES (1, 'Alice', 1000), (2, 'Bob', 2000);");

            stmt.execute("CREATE TABLE IF NOT EXISTS employees (id INT, name VARCHAR, salary INT);");
            stmt.execute("DELETE FROM employees;");
            stmt.execute("INSERT INTO employees VALUES (1, 'Alice', 1000), (2, 'Bob', 2000);");

            stmt.execute("CREATE TABLE IF NOT EXISTS remote_table (id INT, name VARCHAR, salary INT);");
            stmt.execute("DELETE FROM remote_table;");
            stmt.execute("INSERT INTO remote_table VALUES (1, 'Alice', 1000), (2, 'Bob', 2000);");
        }
    }

    @Test
    @DisplayName("Should accept JSON body with SQL queries for both DS1 and DS2, write Parquet, and record SQL_QUERY type")
    void shouldUploadBothDatasetsViaSqlJson() throws Exception {
        DatabaseConnectionConfig ds1Conn = new DatabaseConnectionConfig("pg-host-1", 5432, "inventory_db", "user1", "pass1");
        DatabaseConnectionConfig ds2Conn = new DatabaseConnectionConfig("pg-host-2", 5432, "warehouse_db", "user2", "pass2");

        UploadConfigRequest request = new UploadConfigRequest(
                null,
                null,
                "SELECT id, name, salary FROM staff",
                ds1Conn,
                "SELECT id, name, salary FROM employees",
                ds2Conn
        );

        MvcResult result = mockMvc.perform(post("/api/v1/comparisons/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comparisonId").isNotEmpty())
                .andExpect(jsonPath("$.columns.ds1[0]").value("ID"))
                .andExpect(jsonPath("$.columns.ds1[1]").value("NAME"))
                .andExpect(jsonPath("$.columns.ds1[2]").value("SALARY"))
                .andExpect(jsonPath("$.columns.ds2[0]").value("ID"))
                .andExpect(jsonPath("$.columns.ds2[1]").value("NAME"))
                .andExpect(jsonPath("$.columns.ds2[2]").value("SALARY"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Map<?, ?> responseMap = objectMapper.readValue(responseBody, Map.class);
        String comparisonId = (String) responseMap.get("comparisonId");

        Optional<ComparisonRecord> recordOpt = comparisonRepository.findById(comparisonId);
        assertThat(recordOpt).isPresent();
        ComparisonRecord record = recordOpt.get();
        assertThat(record.getStatus()).isEqualTo(ComparisonStatus.UPLOADED);
        assertThat(record.getDs1Type()).isEqualTo(DataSourceType.SQL_QUERY);
        assertThat(record.getDs1FileName()).isEqualTo("PostgreSQL: inventory_db");
        assertThat(record.getDs2Type()).isEqualTo(DataSourceType.SQL_QUERY);
        assertThat(record.getDs2FileName()).isEqualTo("PostgreSQL: warehouse_db");

        Path storageDir = Path.of(appProperties.storage().path(), comparisonId);
        assertThat(Files.exists(storageDir.resolve("ds1.parquet"))).isTrue();
        assertThat(Files.exists(storageDir.resolve("ds2.parquet"))).isTrue();
    }

    @Test
    @DisplayName("Should accept mixed mode: DS1 from uploaded CSV file and DS2 from PostgreSQL SQL query")
    void shouldUploadMixedFileAndSql() throws Exception {
        String ds1Csv = "id,name,salary\n1,Alice,1000\n2,Bob,2000\n";
        MockMultipartFile ds1File = new MockMultipartFile(
                "ds1File", "dataset1.csv", "text/csv", ds1Csv.getBytes(StandardCharsets.UTF_8));

        DatabaseConnectionConfig ds2Conn = new DatabaseConnectionConfig("pg-host", 5432, "target_db", "user", "pass");
        UploadConfigRequest config = new UploadConfigRequest(
                "auto",
                "auto",
                null,
                null,
                "SELECT id, name, salary FROM remote_table",
                ds2Conn
        );

        MockMultipartFile configPart = new MockMultipartFile(
                "config", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(config));

        MvcResult result = mockMvc.perform(multipart("/api/v1/comparisons/upload")
                        .file(ds1File)
                        .file(configPart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comparisonId").isNotEmpty())
                .andExpect(jsonPath("$.columns.ds1[0]").value("id"))
                .andExpect(jsonPath("$.columns.ds2[0]").value("ID"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Map<?, ?> responseMap = objectMapper.readValue(responseBody, Map.class);
        String comparisonId = (String) responseMap.get("comparisonId");

        ComparisonRecord record = comparisonRepository.findById(comparisonId).orElseThrow();
        assertThat(record.getDs1Type()).isEqualTo(DataSourceType.FILE_UPLOAD);
        assertThat(record.getDs1FileName()).isEqualTo("dataset1.csv");
        assertThat(record.getDs2Type()).isEqualTo(DataSourceType.SQL_QUERY);
        assertThat(record.getDs2FileName()).isEqualTo("PostgreSQL: target_db");
    }

    @Test
    @DisplayName("Should accept mixed mode: DS1 from SQL query and DS2 from uploaded CSV file")
    void shouldUploadMixedSqlAndFile() throws Exception {
        String ds2Csv = "id,name,salary\n1,Alice,1000\n2,Bob,2000\n";
        MockMultipartFile ds2File = new MockMultipartFile(
                "ds2File", "dataset2.csv", "text/csv", ds2Csv.getBytes(StandardCharsets.UTF_8));

        DatabaseConnectionConfig ds1Conn = new DatabaseConnectionConfig("pg-host", 5432, "source_db", "user", "pass");
        UploadConfigRequest config = new UploadConfigRequest(
                "auto",
                "auto",
                "SELECT id, name, salary FROM staff",
                ds1Conn,
                null,
                null
        );

        MockMultipartFile configPart = new MockMultipartFile(
                "config", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(config));

        MvcResult result = mockMvc.perform(multipart("/api/v1/comparisons/upload")
                        .file(ds2File)
                        .file(configPart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comparisonId").isNotEmpty())
                .andExpect(jsonPath("$.columns.ds1[0]").value("ID"))
                .andExpect(jsonPath("$.columns.ds2[0]").value("id"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Map<?, ?> responseMap = objectMapper.readValue(responseBody, Map.class);
        String comparisonId = (String) responseMap.get("comparisonId");

        ComparisonRecord record = comparisonRepository.findById(comparisonId).orElseThrow();
        assertThat(record.getDs1Type()).isEqualTo(DataSourceType.SQL_QUERY);
        assertThat(record.getDs1FileName()).isEqualTo("PostgreSQL: source_db");
        assertThat(record.getDs2Type()).isEqualTo(DataSourceType.FILE_UPLOAD);
        assertThat(record.getDs2FileName()).isEqualTo("dataset2.csv");
    }

    @Test
    @DisplayName("Should reject DML/DDL queries with 400 Bad Request")
    void shouldRejectDmlQueriesInUpload() throws Exception {
        DatabaseConnectionConfig connConfig = new DatabaseConnectionConfig("host", 5432, "db", "user", "pass");
        UploadConfigRequest request = new UploadConfigRequest(
                null,
                null,
                "DROP TABLE users",
                connConfig,
                "SELECT * FROM users",
                connConfig
        );

        mockMvc.perform(post("/api/v1/comparisons/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when database connection fails")
    void shouldHandleConnectionErrorInUpload() throws Exception {
        DatabaseConnectionConfig failConfig = new DatabaseConnectionConfig("unreachable", 5432, "db", "user", "pass");

        UploadConfigRequest request = new UploadConfigRequest(
                null,
                null,
                "SELECT * FROM staff",
                failConfig,
                "SELECT * FROM staff",
                failConfig
        );

        mockMvc.perform(post("/api/v1/comparisons/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 408 Request Timeout when query execution times out")
    void shouldHandleQueryTimeoutInUpload() throws Exception {
        DatabaseConnectionConfig timeoutConfig = new DatabaseConnectionConfig("timeout_host", 5432, "db", "user", "pass");

        UploadConfigRequest request = new UploadConfigRequest(
                null,
                null,
                "SELECT * FROM staff",
                timeoutConfig,
                "SELECT * FROM staff",
                timeoutConfig
        );

        mockMvc.perform(post("/api/v1/comparisons/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isRequestTimeout());
    }
}
