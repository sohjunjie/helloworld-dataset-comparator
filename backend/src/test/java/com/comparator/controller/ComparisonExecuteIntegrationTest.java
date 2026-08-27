package com.comparator.controller;

import com.comparator.config.AppProperties;
import com.comparator.model.dto.ComparisonExecuteRequest;
import com.comparator.model.dto.UploadResponse;
import com.comparator.model.enums.ComparisonStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ComparisonExecuteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppProperties appProperties;

    @Test
    @DisplayName("End-to-End: Upload 2 CSVs -> Execute Comparison -> Verify summary counts and Parquet files")
    void testEndToEndComparisonFlow() throws Exception {
        // 1. Prepare CSV fixtures
        // DS1 has: id 1 (exact match), id 2 (mismatch amount), id 3 (missing from DS2)
        String ds1Csv = "id,name,amount\n1,Alice,100\n2,Bob,200\n3,Charlie,300\n";
        // DS2 has: id 1 (exact match), id 2 (mismatch amount), id 4 (missing from DS1)
        String ds2Csv = "id,name,amount\n1,Alice,100\n2,Bob,250\n4,David,400\n";

        MockMultipartFile file1 = new MockMultipartFile("ds1File", "dataset1.csv", "text/csv", ds1Csv.getBytes());
        MockMultipartFile file2 = new MockMultipartFile("ds2File", "dataset2.csv", "text/csv", ds2Csv.getBytes());

        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/comparisons/upload")
                        .file(file1)
                        .file(file2)
                        .param("ds1Delimiter", ",")
                        .param("ds2Delimiter", ","))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comparisonId").isNotEmpty())
                .andExpect(jsonPath("$.columns.ds1[0]").value("id"))
                .andReturn();

        UploadResponse uploadResponse = objectMapper.readValue(
                uploadResult.getResponse().getContentAsString(),
                UploadResponse.class
        );
        String comparisonId = uploadResponse.comparisonId();

        // 2. Execute Comparison with key column "id"
        ComparisonExecuteRequest executeRequest = new ComparisonExecuteRequest(List.of("id"));

        mockMvc.perform(post("/api/v1/comparisons/" + comparisonId + "/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(executeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(comparisonId));

        // 3. Await completion and verify summary counts
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            mockMvc.perform(get("/api/v1/comparisons/" + comparisonId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(ComparisonStatus.COMPLETED.name()))
                    .andExpect(jsonPath("$.ds1RecordCount").value(3))
                    .andExpect(jsonPath("$.ds2RecordCount").value(3))
                    .andExpect(jsonPath("$.ds1FullyMatching").value(1))
                    .andExpect(jsonPath("$.ds2FullyMatching").value(1))
                    .andExpect(jsonPath("$.ds1NotMatching").value(1))
                    .andExpect(jsonPath("$.ds2NotMatching").value(1))
                    .andExpect(jsonPath("$.ds1MissingInDs2").value(1))
                    .andExpect(jsonPath("$.ds2MissingInDs1").value(1));
        });

        // 4. Verify Parquet output files on disk
        Path storageDir = Path.of(appProperties.storage().path(), comparisonId);
        assertThat(Files.exists(storageDir.resolve("ds1.parquet"))).isTrue();
        assertThat(Files.exists(storageDir.resolve("ds2.parquet"))).isTrue();
        assertThat(Files.exists(storageDir.resolve("matches.parquet"))).isTrue();
        assertThat(Files.exists(storageDir.resolve("mismatches_ds1.parquet"))).isTrue();
        assertThat(Files.exists(storageDir.resolve("mismatches_ds2.parquet"))).isTrue();
        assertThat(Files.exists(storageDir.resolve("missing_from_ds1.parquet"))).isTrue();
        assertThat(Files.exists(storageDir.resolve("missing_from_ds2.parquet"))).isTrue();
    }

    @Test
    @DisplayName("Should return 404 when executing non-existent comparison")
    void testExecuteNonExistentComparison() throws Exception {
        ComparisonExecuteRequest executeRequest = new ComparisonExecuteRequest(List.of("id"));

        mockMvc.perform(post("/api/v1/comparisons/non-existent-id/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(executeRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when key columns is empty")
    void testExecuteEmptyKeyColumns() throws Exception {
        // Upload fixture first
        MockMultipartFile file1 = new MockMultipartFile("ds1File", "d1.csv", "text/csv", "id\n1\n".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("ds2File", "d2.csv", "text/csv", "id\n1\n".getBytes());

        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/comparisons/upload")
                        .file(file1)
                        .file(file2))
                .andExpect(status().isOk())
                .andReturn();

        UploadResponse uploadResponse = objectMapper.readValue(
                uploadResult.getResponse().getContentAsString(),
                UploadResponse.class
        );

        mockMvc.perform(post("/api/v1/comparisons/" + uploadResponse.comparisonId() + "/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"keyColumns\": []}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should accept array of string key columns directly in request body")
    void testExecuteDirectArrayOfKeyColumns() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("ds1File", "d1.csv", "text/csv", "id,val\n1,A\n".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("ds2File", "d2.csv", "text/csv", "id,val\n1,A\n".getBytes());

        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/comparisons/upload")
                        .file(file1)
                        .file(file2))
                .andExpect(status().isOk())
                .andReturn();

        UploadResponse uploadResponse = objectMapper.readValue(
                uploadResult.getResponse().getContentAsString(),
                UploadResponse.class
        );
        String comparisonId = uploadResponse.comparisonId();

        mockMvc.perform(post("/api/v1/comparisons/" + comparisonId + "/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"id\"]"))
                .andExpect(status().isOk());

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            mockMvc.perform(get("/api/v1/comparisons/" + comparisonId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(ComparisonStatus.COMPLETED.name()))
                    .andExpect(jsonPath("$.ds1FullyMatching").value(1));
        });
    }

    @Test
    @DisplayName("End-to-End: Tolerance matching at boundary and fallback for non-numeric values")
    void testToleranceMatchAtBoundaryAndFallbackNonNumeric() throws Exception {
        // DS1:
        // row 1: id=1, score=100.0 (boundary match with 105.0 at 5%)
        // row 2: id=2, score=100.0 (reverse boundary match with 105.26 at 5% on DS2)
        // row 3: id=3, score=N/A (fallback exact match with N/A)
        // row 4: id=4, score=100.0 (outside 5% boundary with 106.0 -> mismatch)
        // row 5: id=5, score=N/A (fallback mismatch with 100.0)
        String ds1Csv = "id,score\n1,100\n2,100\n3,N/A\n4,100\n5,N/A\n";
        String ds2Csv = "id,score\n1,105\n2,105.26\n3,N/A\n4,106\n5,100\n";

        MockMultipartFile file1 = new MockMultipartFile("ds1File", "d1.csv", "text/csv", ds1Csv.getBytes());
        MockMultipartFile file2 = new MockMultipartFile("ds2File", "d2.csv", "text/csv", ds2Csv.getBytes());

        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/comparisons/upload")
                        .file(file1)
                        .file(file2))
                .andExpect(status().isOk())
                .andReturn();

        UploadResponse uploadResponse = objectMapper.readValue(
                uploadResult.getResponse().getContentAsString(),
                UploadResponse.class
        );
        String comparisonId = uploadResponse.comparisonId();

        String executePayload = """
                {
                    "keyColumns": ["id"],
                    "tolerances": [
                        {"columnName": "score", "percentage": 5.0}
                    ],
                    "caseSensitive": true
                }
                """;

        mockMvc.perform(post("/api/v1/comparisons/" + comparisonId + "/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(executePayload))
                .andExpect(status().isOk());

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            mockMvc.perform(get("/api/v1/comparisons/" + comparisonId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(ComparisonStatus.COMPLETED.name()))
                    .andExpect(jsonPath("$.ds1RecordCount").value(5))
                    .andExpect(jsonPath("$.ds2RecordCount").value(5))
                    .andExpect(jsonPath("$.ds1FullyMatching").value(3)) // rows 1, 2, 3
                    .andExpect(jsonPath("$.ds2FullyMatching").value(3))
                    .andExpect(jsonPath("$.ds1NotMatching").value(2))   // rows 4, 5
                    .andExpect(jsonPath("$.ds2NotMatching").value(2));
        });
    }

    @Test
    @DisplayName("Should return 400 when tolerance percentage is outside 0 to 100 range")
    void testToleranceValidationRejectsInvalidPercentages() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("ds1File", "d1.csv", "text/csv", "id,val\n1,10\n".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("ds2File", "d2.csv", "text/csv", "id,val\n1,10\n".getBytes());

        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/comparisons/upload")
                        .file(file1)
                        .file(file2))
                .andExpect(status().isOk())
                .andReturn();

        UploadResponse uploadResponse = objectMapper.readValue(
                uploadResult.getResponse().getContentAsString(),
                UploadResponse.class
        );
        String comparisonId = uploadResponse.comparisonId();

        // Negative tolerance percentage
        String invalidNegativePayload = """
                {
                    "keyColumns": ["id"],
                    "tolerances": [
                        {"columnName": "val", "percentage": -5.0}
                    ]
                }
                """;

        mockMvc.perform(post("/api/v1/comparisons/" + comparisonId + "/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidNegativePayload))
                .andExpect(status().isBadRequest());

        // Over 100% tolerance percentage
        String invalidOverHundredPayload = """
                {
                    "keyColumns": ["id"],
                    "tolerances": [
                        {"columnName": "val", "percentage": 105.0}
                    ]
                }
                """;

        mockMvc.perform(post("/api/v1/comparisons/" + comparisonId + "/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidOverHundredPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("End-to-End: Case-insensitive string comparison toggle")
    void testCaseInsensitiveComparisonToggle() throws Exception {
        String ds1Csv = "id,name,city\nK1,Alice,New York\nK2,Bob,London\n";
        String ds2Csv = "id,name,city\nk1,ALICE,NEW YORK\nk2,bob,LONDON\n";

        MockMultipartFile file1 = new MockMultipartFile("ds1File", "d1.csv", "text/csv", ds1Csv.getBytes());
        MockMultipartFile file2 = new MockMultipartFile("ds2File", "d2.csv", "text/csv", ds2Csv.getBytes());

        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/comparisons/upload")
                        .file(file1)
                        .file(file2))
                .andExpect(status().isOk())
                .andReturn();

        UploadResponse uploadResponse = objectMapper.readValue(
                uploadResult.getResponse().getContentAsString(),
                UploadResponse.class
        );
        String comparisonId = uploadResponse.comparisonId();

        String executePayload = """
                {
                    "keyColumns": ["id"],
                    "caseSensitive": false
                }
                """;

        mockMvc.perform(post("/api/v1/comparisons/" + comparisonId + "/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(executePayload))
                .andExpect(status().isOk());

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            mockMvc.perform(get("/api/v1/comparisons/" + comparisonId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(ComparisonStatus.COMPLETED.name()))
                    .andExpect(jsonPath("$.ds1FullyMatching").value(2))
                    .andExpect(jsonPath("$.ds1NotMatching").value(0));
        });
    }

    @Test
    @DisplayName("End-to-End: Schema unification when datasets have mismatched columns")
    void testMismatchedColumnSchemas() throws Exception {
        // DS1 has [id, name, extra_ds1]
        // DS2 has [id, name, extra_ds2]
        // row 1: non-null extra_ds1 ('val1') vs NULL in DS2 -> mismatch
        // row 2: empty extra_ds1 (NULL) and empty extra_ds2 (NULL) -> match
        String ds1Csv = "id,name,extra_ds1\n1,Alice,val1\n2,Bob,\n";
        String ds2Csv = "id,name,extra_ds2\n1,Alice,val2\n2,Bob,\n";

        MockMultipartFile file1 = new MockMultipartFile("ds1File", "d1.csv", "text/csv", ds1Csv.getBytes());
        MockMultipartFile file2 = new MockMultipartFile("ds2File", "d2.csv", "text/csv", ds2Csv.getBytes());

        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/comparisons/upload")
                        .file(file1)
                        .file(file2))
                .andExpect(status().isOk())
                .andReturn();

        UploadResponse uploadResponse = objectMapper.readValue(
                uploadResult.getResponse().getContentAsString(),
                UploadResponse.class
        );
        String comparisonId = uploadResponse.comparisonId();

        String executePayload = """
                {
                    "keyColumns": ["id"]
                }
                """;

        mockMvc.perform(post("/api/v1/comparisons/" + comparisonId + "/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(executePayload))
                .andExpect(status().isOk());

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            mockMvc.perform(get("/api/v1/comparisons/" + comparisonId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(ComparisonStatus.COMPLETED.name()))
                    .andExpect(jsonPath("$.ds1RecordCount").value(2))
                    .andExpect(jsonPath("$.ds2RecordCount").value(2))
                    .andExpect(jsonPath("$.ds1FullyMatching").value(1))
                    .andExpect(jsonPath("$.ds1NotMatching").value(1));
        });
    }

    @Test
    @DisplayName("End-to-End: Duplicate-key cross-comparison (cartesian product per key)")
    void testDuplicateKeyCrossComparison() throws Exception {
        // DS1 has 2 rows with id=1 (10, 20)
        // DS2 has 2 rows with id=1 (10, 30)
        String ds1Csv = "id,val\n1,10\n1,20\n";
        String ds2Csv = "id,val\n1,10\n1,30\n";

        MockMultipartFile file1 = new MockMultipartFile("ds1File", "d1.csv", "text/csv", ds1Csv.getBytes());
        MockMultipartFile file2 = new MockMultipartFile("ds2File", "d2.csv", "text/csv", ds2Csv.getBytes());

        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/comparisons/upload")
                        .file(file1)
                        .file(file2))
                .andExpect(status().isOk())
                .andReturn();

        UploadResponse uploadResponse = objectMapper.readValue(
                uploadResult.getResponse().getContentAsString(),
                UploadResponse.class
        );
        String comparisonId = uploadResponse.comparisonId();

        String executePayload = """
                {
                    "keyColumns": ["id"]
                }
                """;

        mockMvc.perform(post("/api/v1/comparisons/" + comparisonId + "/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(executePayload))
                .andExpect(status().isOk());

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            mockMvc.perform(get("/api/v1/comparisons/" + comparisonId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(ComparisonStatus.COMPLETED.name()))
                    .andExpect(jsonPath("$.ds1RecordCount").value(2))
                    .andExpect(jsonPath("$.ds2RecordCount").value(2))
                    .andExpect(jsonPath("$.ds1FullyMatching").value(1))
                    .andExpect(jsonPath("$.ds2FullyMatching").value(1))
                    .andExpect(jsonPath("$.ds1NotMatching").value(3))
                    .andExpect(jsonPath("$.ds2NotMatching").value(3));
        });
    }
}
