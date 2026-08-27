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
}
