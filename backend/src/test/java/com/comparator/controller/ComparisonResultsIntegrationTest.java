package com.comparator.controller;

import com.comparator.model.dto.ComparisonExecuteRequest;
import com.comparator.model.dto.UploadResponse;
import com.comparator.model.enums.ComparisonStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ComparisonResultsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String comparisonId;

    @BeforeEach
    void setUp() throws Exception {
        // Prepare datasets with:
        // Matches:
        //   id 1, 2, 3 (identical name and amount)
        // Mismatches (5 rows):
        //   id 10: name differs (Alice vs AliceUpdated)
        //   id 11: amount differs (100 vs 150)
        //   id 12: city differs (NY vs SF)
        //   id 13: status differs (ACTIVE vs INACTIVE)
        //   id 14: multiple columns differ (Bob, 200, London vs Robert, 250, Paris)
        // Missing from DS2 (present only in DS1, 2 rows):
        //   id 20, id 21
        // Missing from DS1 (present only in DS2, 2 rows):
        //   id 30, id 31

        String ds1Csv = """
                id,name,amount,city,status
                1,MatchOne,100,Chicago,OPEN
                2,MatchTwo,200,Chicago,OPEN
                3,MatchThree,300,Chicago,OPEN
                10,Alice,500,Boston,ACTIVE
                11,Bob,100,Denver,ACTIVE
                12,Charlie,700,NY,ACTIVE
                13,Dave,800,Seattle,ACTIVE
                14,Bob,200,London,ACTIVE
                20,OnlyInDs1A,1000,Austin,NEW
                21,OnlyInDs1B,1100,Dallas,NEW
                """;

        String ds2Csv = """
                id,name,amount,city,status
                1,MatchOne,100,Chicago,OPEN
                2,MatchTwo,200,Chicago,OPEN
                3,MatchThree,300,Chicago,OPEN
                10,AliceUpdated,500,Boston,ACTIVE
                11,Bob,150,Denver,ACTIVE
                12,Charlie,700,SF,ACTIVE
                13,Dave,800,Seattle,INACTIVE
                14,Robert,250,Paris,CLOSED
                30,OnlyInDs2A,2000,Miami,NEW
                31,OnlyInDs2B,2100,Orlando,NEW
                """;

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
        comparisonId = uploadResponse.comparisonId();

        ComparisonExecuteRequest executeRequest = new ComparisonExecuteRequest(List.of("id"));

        mockMvc.perform(post("/api/v1/comparisons/" + comparisonId + "/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(executeRequest)))
                .andExpect(status().isOk());

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            mockMvc.perform(get("/api/v1/comparisons/" + comparisonId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(ComparisonStatus.COMPLETED.name()))
                    .andExpect(jsonPath("$.ds1RecordCount").value(10))
                    .andExpect(jsonPath("$.ds2RecordCount").value(10))
                    .andExpect(jsonPath("$.ds1FullyMatching").value(3))
                    .andExpect(jsonPath("$.ds1NotMatching").value(5))
                    .andExpect(jsonPath("$.ds1MissingInDs2").value(2))
                    .andExpect(jsonPath("$.ds2MissingInDs1").value(2));
        });
    }

    @Test
    @DisplayName("GET /api/v1/comparisons/{id}/results/mismatches paginates correctly with page boundaries and structure")
    void testPaginateMismatches() throws Exception {
        // Page 0, Size 2 -> 2 items, totalElements=5, totalPages=3, last=false
        mockMvc.perform(get("/api/v1/comparisons/" + comparisonId + "/results/mismatches")
                        .param("page", "0")
                        .param("size", "2")
                        .param("direction", "ds1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].keyValues.id").value("10"))
                .andExpect(jsonPath("$.content[0].ds1Values.name").value("Alice"))
                .andExpect(jsonPath("$.content[0].ds2Values.name").value("AliceUpdated"))
                .andExpect(jsonPath("$.content[0].differingColumns").value(containsInAnyOrder("name")))
                .andExpect(jsonPath("$.content[0].dataDs1.name").value("Alice"))
                .andExpect(jsonPath("$.content[0].dataDs2.name").value("AliceUpdated"))
                .andExpect(jsonPath("$.content[1].keyValues.id").value("11"))
                .andExpect(jsonPath("$.content[1].differingColumns").value(containsInAnyOrder("amount")));

        // Page 1, Size 2 -> 2 items, totalElements=5, totalPages=3, last=false
        mockMvc.perform(get("/api/v1/comparisons/" + comparisonId + "/results/mismatches")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].keyValues.id").value("12"))
                .andExpect(jsonPath("$.content[1].keyValues.id").value("13"));

        // Page 2, Size 2 -> 1 item (last page), totalElements=5, totalPages=3, last=true
        mockMvc.perform(get("/api/v1/comparisons/" + comparisonId + "/results/mismatches")
                        .param("page", "2")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].keyValues.id").value("14"))
                .andExpect(jsonPath("$.content[0].differingColumns").value(containsInAnyOrder("name", "amount", "city", "status")));

        // Page 3, Size 2 -> 0 items (beyond last page), totalElements=5, totalPages=3, last=true
        mockMvc.perform(get("/api/v1/comparisons/" + comparisonId + "/results/mismatches")
                        .param("page", "3")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(3))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content", empty()));
    }

    @Test
    @DisplayName("GET /api/v1/comparisons/{id}/results/missing with direction ds1 and ds2")
    void testPaginateMissing() throws Exception {
        // Direction ds1 (records in DS1 missing from DS2)
        mockMvc.perform(get("/api/v1/comparisons/" + comparisonId + "/results/missing")
                        .param("page", "0")
                        .param("size", "50")
                        .param("direction", "ds1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].keyValues.id").value("20"))
                .andExpect(jsonPath("$.content[0].values.name").value("OnlyInDs1A"))
                .andExpect(jsonPath("$.content[0].missingFrom").value("DS2"))
                .andExpect(jsonPath("$.content[0].direction").value("DS1"))
                .andExpect(jsonPath("$.content[1].keyValues.id").value("21"))
                .andExpect(jsonPath("$.content[1].values.name").value("OnlyInDs1B"));

        // Direction ds2 (records in DS2 missing from DS1)
        mockMvc.perform(get("/api/v1/comparisons/" + comparisonId + "/results/missing")
                        .param("page", "0")
                        .param("size", "50")
                        .param("direction", "ds2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].keyValues.id").value("30"))
                .andExpect(jsonPath("$.content[0].values.name").value("OnlyInDs2A"))
                .andExpect(jsonPath("$.content[0].missingFrom").value("DS1"))
                .andExpect(jsonPath("$.content[0].direction").value("DS2"))
                .andExpect(jsonPath("$.content[1].keyValues.id").value("31"))
                .andExpect(jsonPath("$.content[1].values.name").value("OnlyInDs2B"));
    }

    @Test
    @DisplayName("GET /api/v1/comparisons/{id}/results/matches returns matching record pairs")
    void testPaginateMatches() throws Exception {
        mockMvc.perform(get("/api/v1/comparisons/" + comparisonId + "/results/matches")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value("1"))
                .andExpect(jsonPath("$.content[0].name").value("MatchOne"))
                .andExpect(jsonPath("$.content[0]._row_id").doesNotExist())
                .andExpect(jsonPath("$.content[1].id").value("2"));

        // Page 1
        mockMvc.perform(get("/api/v1/comparisons/" + comparisonId + "/results/matches")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value("3"))
                .andExpect(jsonPath("$.content[0].name").value("MatchThree"));
    }

    @Test
    @DisplayName("Backward-compatible routes without /results prefix")
    void testRouteAliases() throws Exception {
        mockMvc.perform(get("/api/v1/comparisons/" + comparisonId + "/mismatches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(5));

        mockMvc.perform(get("/api/v1/comparisons/" + comparisonId + "/missing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(get("/api/v1/comparisons/" + comparisonId + "/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));

        mockMvc.perform(get("/api/comparisons/" + comparisonId + "/results/mismatches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(5));
    }

    @Test
    @DisplayName("Should return 404 when comparison not found or uncompleted")
    void test404Handling() throws Exception {
        // Non-existent comparison
        mockMvc.perform(get("/api/v1/comparisons/non-existent-123/results/mismatches"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/comparisons/non-existent-123/results/missing"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/comparisons/non-existent-123/results/matches"))
                .andExpect(status().isNotFound());

        // Upload new comparison (status = UPLOADED, not COMPLETED)
        MockMultipartFile file1 = new MockMultipartFile("ds1File", "d1.csv", "text/csv", "id\n1\n".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("ds2File", "d2.csv", "text/csv", "id\n1\n".getBytes());
        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/comparisons/upload")
                        .file(file1)
                        .file(file2))
                .andExpect(status().isOk())
                .andReturn();
        String pendingId = objectMapper.readValue(uploadResult.getResponse().getContentAsString(), UploadResponse.class).comparisonId();

        mockMvc.perform(get("/api/v1/comparisons/" + pendingId + "/results/mismatches"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/comparisons/" + pendingId + "/results/missing"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/comparisons/" + pendingId + "/results/matches"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should handle empty result sets gracefully when 0 mismatches or missing")
    void testEmptyResultSets() throws Exception {
        // Perfect match datasets
        String ds1Csv = "id,name\n1,Alice\n2,Bob\n";
        String ds2Csv = "id,name\n1,Alice\n2,Bob\n";

        MockMultipartFile file1 = new MockMultipartFile("ds1File", "d1.csv", "text/csv", ds1Csv.getBytes());
        MockMultipartFile file2 = new MockMultipartFile("ds2File", "d2.csv", "text/csv", ds2Csv.getBytes());

        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/comparisons/upload")
                        .file(file1)
                        .file(file2))
                .andExpect(status().isOk())
                .andReturn();
        String perfectCompId = objectMapper.readValue(uploadResult.getResponse().getContentAsString(), UploadResponse.class).comparisonId();

        mockMvc.perform(post("/api/v1/comparisons/" + perfectCompId + "/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"keyColumns\":[\"id\"]}"))
                .andExpect(status().isOk());

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            mockMvc.perform(get("/api/v1/comparisons/" + perfectCompId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(ComparisonStatus.COMPLETED.name()))
                    .andExpect(jsonPath("$.ds1FullyMatching").value(2));
        });

        // 0 mismatches
        mockMvc.perform(get("/api/v1/comparisons/" + perfectCompId + "/results/mismatches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content", empty()));

        // 0 missing in DS2
        mockMvc.perform(get("/api/v1/comparisons/" + perfectCompId + "/results/missing?direction=ds1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content", empty()));

        // 0 missing in DS1
        mockMvc.perform(get("/api/v1/comparisons/" + perfectCompId + "/results/missing?direction=ds2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content", empty()));
    }
}
