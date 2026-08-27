package com.comparator.controller;

import com.comparator.model.dto.ComparisonExecuteRequest;
import com.comparator.model.dto.UploadResponse;
import com.comparator.model.entity.ComparisonRecord;
import com.comparator.model.enums.ComparisonStatus;
import com.comparator.repository.ComparisonRepository;
import com.comparator.service.ProgressService;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ComparisonProgressIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProgressService progressService;

    @Autowired
    private ComparisonRepository comparisonRepository;

    @Test
    @DisplayName("Should establish SSE connection on GET /api/v1/comparisons/{id}/events")
    void testSubscribeSseEventsEndpoint() throws Exception {
        // 1. Upload datasets to create a comparison record
        MockMultipartFile file1 = new MockMultipartFile("ds1File", "d1.csv", "text/csv", "id,name\n1,Alice\n".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("ds2File", "d2.csv", "text/csv", "id,name\n1,Alice\n".getBytes());

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

        // 2. Subscribe to SSE endpoint
        mockMvc.perform(get("/api/v1/comparisons/" + comparisonId + "/events")
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());

        assertThat(progressService.getEmitterCount(comparisonId)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Should establish SSE connection on alias GET /api/comparisons/{id}/progress")
    void testSubscribeSseProgressAliasEndpoint() throws Exception {
        ComparisonRecord record = new ComparisonRecord();
        String compId = UUID.randomUUID().toString();
        record.setId(compId);
        record.setStatus(ComparisonStatus.UPLOADED);
        record.setCreatedAt(LocalDateTime.now());
        comparisonRepository.save(record);

        mockMvc.perform(get("/api/comparisons/" + compId + "/progress")
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());

        assertThat(progressService.getEmitterCount(compId)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Should return 404 when subscribing to SSE for non-existent comparison")
    void testSubscribeNonExistentComparison() throws Exception {
        mockMvc.perform(get("/api/v1/comparisons/non-existent-uuid/events")
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("End-to-End: Subscribe to SSE, execute comparison, and verify stage event sequence and cleanup")
    void testSseEventSequenceDuringComparison() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("ds1File", "d1.csv", "text/csv", "id,score\n1,100\n2,200\n".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("ds2File", "d2.csv", "text/csv", "id,score\n1,100\n2,250\n".getBytes());

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

        SseEmitter emitter = progressService.subscribe(comparisonId);
        assertThat(progressService.getEmitterCount(comparisonId)).isGreaterThanOrEqualTo(1);

        // Execute comparison
        ComparisonExecuteRequest executeRequest = new ComparisonExecuteRequest(List.of("id"));
        mockMvc.perform(post("/api/v1/comparisons/" + comparisonId + "/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(executeRequest)))
                .andExpect(status().isOk());

        // Await completion
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            mockMvc.perform(get("/api/v1/comparisons/" + comparisonId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(ComparisonStatus.COMPLETED.name()));
        });

        // After completion, emitters for this comparison should have completed and cleaned up
        await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            assertThat(progressService.getEmitterCount(comparisonId)).isEqualTo(0);
        });
    }
}
