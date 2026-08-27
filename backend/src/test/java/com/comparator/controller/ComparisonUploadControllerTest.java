package com.comparator.controller;

import com.comparator.config.AppProperties;
import com.comparator.model.entity.ComparisonRecord;
import com.comparator.model.enums.ComparisonStatus;
import com.comparator.model.enums.DataSourceType;
import com.comparator.repository.ComparisonRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ComparisonUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ComparisonRepository comparisonRepository;

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should successfully upload comma-delimited files, convert to Parquet, and return column headers")
    void shouldUploadCommaDelimitedFiles() throws Exception {
        String ds1Content = "id,name,department\n1,Alice,Engineering\n2,Bob,Sales\n";
        String ds2Content = "id,name,role,salary\n1,Alice,Dev,100\n2,Bob,Rep,90\n";

        MockMultipartFile ds1File = new MockMultipartFile(
                "ds1File", "ds1.csv", "text/csv", ds1Content.getBytes(StandardCharsets.UTF_8));
        MockMultipartFile ds2File = new MockMultipartFile(
                "ds2File", "ds2.csv", "text/csv", ds2Content.getBytes(StandardCharsets.UTF_8));

        MvcResult result = mockMvc.perform(multipart("/api/v1/comparisons/upload")
                        .file(ds1File)
                        .file(ds2File))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comparisonId").isNotEmpty())
                .andExpect(jsonPath("$.columns.ds1[0]").value("id"))
                .andExpect(jsonPath("$.columns.ds1[1]").value("name"))
                .andExpect(jsonPath("$.columns.ds1[2]").value("department"))
                .andExpect(jsonPath("$.columns.ds2[0]").value("id"))
                .andExpect(jsonPath("$.columns.ds2[1]").value("name"))
                .andExpect(jsonPath("$.columns.ds2[2]").value("role"))
                .andExpect(jsonPath("$.columns.ds2[3]").value("salary"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Map<?, ?> responseMap = objectMapper.readValue(responseBody, Map.class);
        String comparisonId = (String) responseMap.get("comparisonId");

        // Verify entity persisted in repository
        Optional<ComparisonRecord> recordOpt = comparisonRepository.findById(comparisonId);
        assertThat(recordOpt).isPresent();
        ComparisonRecord record = recordOpt.get();
        assertThat(record.getStatus()).isEqualTo(ComparisonStatus.UPLOADED);
        assertThat(record.getDs1Type()).isEqualTo(DataSourceType.FILE_UPLOAD);
        assertThat(record.getDs1FileName()).isEqualTo("ds1.csv");
        assertThat(record.getDs2Type()).isEqualTo(DataSourceType.FILE_UPLOAD);
        assertThat(record.getDs2FileName()).isEqualTo("ds2.csv");

        // Verify Parquet files exist on disk
        Path storageDir = Path.of(appProperties.storage().path(), comparisonId);
        Path ds1Parquet = storageDir.resolve("ds1.parquet");
        Path ds2Parquet = storageDir.resolve("ds2.parquet");

        assertThat(Files.exists(ds1Parquet)).isTrue();
        assertThat(Files.exists(ds2Parquet)).isTrue();
        assertThat(Files.size(ds1Parquet)).isGreaterThan(0);
        assertThat(Files.size(ds2Parquet)).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should auto-detect tab-delimited and pipe-delimited files")
    void shouldAutoDetectTabAndPipeDelimitedFiles() throws Exception {
        String ds1Content = "order_id\tcustomer\ttotal\n1001\tAcme\t500\n1002\tBeta\t300\n";
        String ds2Content = "order_id|customer|status\n1001|Acme|Shipped\n1002|Beta|Pending\n";

        MockMultipartFile ds1File = new MockMultipartFile(
                "ds1File", "orders.tsv", "text/tab-separated-values", ds1Content.getBytes(StandardCharsets.UTF_8));
        MockMultipartFile ds2File = new MockMultipartFile(
                "ds2File", "orders.psv", "text/plain", ds2Content.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/comparisons/upload")
                        .file(ds1File)
                        .file(ds2File))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns.ds1[0]").value("order_id"))
                .andExpect(jsonPath("$.columns.ds1[1]").value("customer"))
                .andExpect(jsonPath("$.columns.ds1[2]").value("total"))
                .andExpect(jsonPath("$.columns.ds2[0]").value("order_id"))
                .andExpect(jsonPath("$.columns.ds2[1]").value("customer"))
                .andExpect(jsonPath("$.columns.ds2[2]").value("status"));
    }

    @Test
    @DisplayName("Should accept custom delimiter in JSON config part")
    void shouldAcceptCustomDelimiterInConfigPart() throws Exception {
        String ds1Content = "c1~c2~c3\nv1~v2~v3\n";
        String ds2Content = "c1^c2^c4\nv1^v2^v4\n";

        MockMultipartFile ds1File = new MockMultipartFile(
                "ds1File", "custom1.txt", "text/plain", ds1Content.getBytes(StandardCharsets.UTF_8));
        MockMultipartFile ds2File = new MockMultipartFile(
                "ds2File", "custom2.txt", "text/plain", ds2Content.getBytes(StandardCharsets.UTF_8));
        MockMultipartFile configFile = new MockMultipartFile(
                "config", "", MediaType.APPLICATION_JSON_VALUE,
                "{\"ds1Delimiter\":\"~\",\"ds2Delimiter\":\"^\"}".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/comparisons/upload")
                        .file(ds1File)
                        .file(ds2File)
                        .file(configFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns.ds1[0]").value("c1"))
                .andExpect(jsonPath("$.columns.ds1[1]").value("c2"))
                .andExpect(jsonPath("$.columns.ds1[2]").value("c3"))
                .andExpect(jsonPath("$.columns.ds2[0]").value("c1"))
                .andExpect(jsonPath("$.columns.ds2[1]").value("c2"))
                .andExpect(jsonPath("$.columns.ds2[2]").value("c4"));
    }

    @Test
    @DisplayName("Should fallback to comma on ambiguous or inconsistent delimiters")
    void shouldFallbackToCommaOnAmbiguity() throws Exception {
        String ds1Content = "colA,colB\nval1,val2\n";
        String ds2Content = "headerOne,headerTwo\nvalA,valB\n";

        MockMultipartFile ds1File = new MockMultipartFile(
                "ds1File", "ambig1.csv", "text/csv", ds1Content.getBytes(StandardCharsets.UTF_8));
        MockMultipartFile ds2File = new MockMultipartFile(
                "ds2File", "ambig2.csv", "text/csv", ds2Content.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/comparisons/upload")
                        .file(ds1File)
                        .file(ds2File))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns.ds1[0]").value("colA"))
                .andExpect(jsonPath("$.columns.ds1[1]").value("colB"))
                .andExpect(jsonPath("$.columns.ds2[0]").value("headerOne"))
                .andExpect(jsonPath("$.columns.ds2[1]").value("headerTwo"));
    }

    @Test
    @DisplayName("Should also work on /api/comparisons/upload endpoint")
    void shouldWorkOnLegacyPath() throws Exception {
        String ds1Content = "x,y\n1,2\n";
        String ds2Content = "x,y\n3,4\n";

        MockMultipartFile ds1File = new MockMultipartFile(
                "ds1File", "f1.csv", "text/csv", ds1Content.getBytes(StandardCharsets.UTF_8));
        MockMultipartFile ds2File = new MockMultipartFile(
                "ds2File", "f2.csv", "text/csv", ds2Content.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/comparisons/upload")
                        .file(ds1File)
                        .file(ds2File))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comparisonId").isNotEmpty())
                .andExpect(jsonPath("$.columns.ds1[0]").value("x"))
                .andExpect(jsonPath("$.columns.ds2[0]").value("x"));
    }

    @Test
    @DisplayName("Should successfully upload XLSX and XLS files and convert to Parquet")
    void shouldUploadXlsxAndXlsFiles() throws Exception {
        byte[] xlsxBytes = com.comparator.util.ExcelTestUtils.createTestXlsx(java.util.List.of("id", "username", "role"), java.util.List.of(
                java.util.List.of("101", "admin", "SUPERUSER"),
                java.util.List.of("102", "guest", "GUEST")
        ));
        byte[] xlsBytes = com.comparator.util.ExcelTestUtils.createTestXls(java.util.List.of("id", "username", "status"), java.util.List.of(
                java.util.List.of("101", "admin", "ACTIVE"),
                java.util.List.of("102", "guest", "INACTIVE")
        ));

        MockMultipartFile ds1File = new MockMultipartFile(
                "ds1File", "users.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxBytes);
        MockMultipartFile ds2File = new MockMultipartFile(
                "ds2File", "users.xls", "application/vnd.ms-excel", xlsBytes);

        MvcResult result = mockMvc.perform(multipart("/api/v1/comparisons/upload")
                        .file(ds1File)
                        .file(ds2File))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comparisonId").isNotEmpty())
                .andExpect(jsonPath("$.columns.ds1[0]").value("id"))
                .andExpect(jsonPath("$.columns.ds1[1]").value("username"))
                .andExpect(jsonPath("$.columns.ds1[2]").value("role"))
                .andExpect(jsonPath("$.columns.ds2[0]").value("id"))
                .andExpect(jsonPath("$.columns.ds2[1]").value("username"))
                .andExpect(jsonPath("$.columns.ds2[2]").value("status"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Map<?, ?> responseMap = objectMapper.readValue(responseBody, Map.class);
        String comparisonId = (String) responseMap.get("comparisonId");

        Path storageDir = Path.of(appProperties.storage().path(), comparisonId);
        Path ds1Parquet = storageDir.resolve("ds1.parquet");
        Path ds2Parquet = storageDir.resolve("ds2.parquet");

        assertThat(Files.exists(ds1Parquet)).isTrue();
        assertThat(Files.exists(ds2Parquet)).isTrue();
        assertThat(Files.size(ds1Parquet)).isGreaterThan(0);
        assertThat(Files.size(ds2Parquet)).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should successfully upload mixed CSV and XLSX datasets")
    void shouldUploadMixedCsvAndXlsx() throws Exception {
        String ds1Csv = "product_code,price\nA1,10.0\nA2,20.0\n";
        byte[] ds2Xlsx = com.comparator.util.ExcelTestUtils.createTestXlsx(java.util.List.of("product_code", "inventory"), java.util.List.of(
                java.util.List.of("A1", "100"),
                java.util.List.of("A2", "50")
        ));

        MockMultipartFile ds1File = new MockMultipartFile(
                "ds1File", "catalog.csv", "text/csv", ds1Csv.getBytes(StandardCharsets.UTF_8));
        MockMultipartFile ds2File = new MockMultipartFile(
                "ds2File", "stock.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ds2Xlsx);

        mockMvc.perform(multipart("/api/v1/comparisons/upload")
                        .file(ds1File)
                        .file(ds2File))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comparisonId").isNotEmpty())
                .andExpect(jsonPath("$.columns.ds1[0]").value("product_code"))
                .andExpect(jsonPath("$.columns.ds1[1]").value("price"))
                .andExpect(jsonPath("$.columns.ds2[0]").value("product_code"))
                .andExpect(jsonPath("$.columns.ds2[1]").value("inventory"));
    }

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @TestPropertySource(properties = {"app.upload.max-file-size=50B"})
    class MaxFileSizeLimitTest {

        @Autowired
        private MockMvc mockMvc;

        @Test
        @DisplayName("Should return HTTP 413 when uploaded file exceeds app.upload.max-file-size")
        void shouldReturn413WhenFileOversized() throws Exception {
            byte[] oversizedData = new byte[100];
            MockMultipartFile ds1File = new MockMultipartFile("ds1File", "large.csv", "text/csv", oversizedData);
            MockMultipartFile ds2File = new MockMultipartFile("ds2File", "normal.csv", "text/csv", "id,name\n1,A\n".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(multipart("/api/v1/comparisons/upload")
                            .file(ds1File)
                            .file(ds2File))
                    .andExpect(status().isPayloadTooLarge());
        }
    }
}
