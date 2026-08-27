package com.comparator.controller;

import com.comparator.model.dto.ComparisonRequest;
import com.comparator.model.dto.ComparisonSummary;
import com.comparator.model.entity.ComparisonRecord;
import com.comparator.model.enums.ComparisonStatus;
import com.comparator.model.enums.DataSourceType;
import com.comparator.repository.ComparisonRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ComparisonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ComparisonRepository comparisonRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        comparisonRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/comparisons creates comparison record with PENDING status")
    void shouldCreateComparisonRecord() throws Exception {
        ComparisonRequest request = new ComparisonRequest(
                DataSourceType.FILE_UPLOAD,
                "ds1.csv",
                DataSourceType.FILE_UPLOAD,
                "ds2.csv",
                List.of("id"),
                null,
                true,
                null,
                null,
                null,
                null,
                ",",
                ","
        );

        MvcResult result = mockMvc.perform(post("/api/v1/comparisons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.ds1Type").value("FILE_UPLOAD"))
                .andExpect(jsonPath("$.ds1FileName").value("ds1.csv"))
                .andExpect(jsonPath("$.ds2Type").value("FILE_UPLOAD"))
                .andExpect(jsonPath("$.ds2FileName").value("ds2.csv"))
                .andReturn();

        ComparisonSummary summary = objectMapper.readValue(result.getResponse().getContentAsString(), ComparisonSummary.class);
        assertThat(summary.id()).isNotNull();

        // Verify entity in H2 database
        ComparisonRecord persisted = comparisonRepository.findById(summary.id()).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(ComparisonStatus.PENDING);
        assertThat(persisted.getDs1FileName()).isEqualTo("ds1.csv");
    }

    @Test
    @DisplayName("End-to-End round trip: create -> list -> get-by-id")
    void shouldPerformFullRoundTrip() throws Exception {
        // 1. Create first comparison
        ComparisonRequest req1 = new ComparisonRequest(
                DataSourceType.FILE_UPLOAD,
                "first.csv",
                DataSourceType.FILE_UPLOAD,
                "second.csv",
                null, null, null, null, null, null, null, null, null
        );

        MvcResult postResult = mockMvc.perform(post("/api/v1/comparisons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated())
                .andReturn();

        ComparisonSummary created = objectMapper.readValue(postResult.getResponse().getContentAsString(), ComparisonSummary.class);
        String comparisonId = created.id();

        // 2. List comparisons
        mockMvc.perform(get("/api/v1/comparisons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(comparisonId))
                .andExpect(jsonPath("$[0].ds1FileName").value("first.csv"));

        // 3. Get by ID
        mockMvc.perform(get("/api/v1/comparisons/" + comparisonId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(comparisonId))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.ds1FileName").value("first.csv"));
    }

    @Test
    @DisplayName("GET /api/v1/comparisons/{id} returns 404 when not found")
    void shouldReturn404ForNonExistentComparison() throws Exception {
        mockMvc.perform(get("/api/v1/comparisons/non-existent-id"))
                .andExpect(status().isNotFound());
    }
}
