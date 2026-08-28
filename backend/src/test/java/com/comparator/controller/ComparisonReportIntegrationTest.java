package com.comparator.controller;

import com.comparator.model.dto.ComparisonExecuteRequest;
import com.comparator.model.dto.ToleranceConfig;
import com.comparator.model.dto.UploadResponse;
import com.comparator.model.enums.ComparisonStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ComparisonReportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String completedComparisonId;

    @BeforeEach
    void setUp() throws Exception {
        String ds1Csv = """
                id,name,amount,city,status
                1,MatchOne,100,Chicago,OPEN
                2,MatchTwo,200,Chicago,OPEN
                10,Alice,500,Boston,ACTIVE
                11,Bob,100,Denver,ACTIVE
                20,OnlyInDs1A,1000,Austin,NEW
                """;

        String ds2Csv = """
                id,name,amount,city,status
                1,MatchOne,100,Chicago,OPEN
                2,MatchTwo,200,Chicago,OPEN
                10,AliceUpdated,500,Boston,ACTIVE
                11,Bob,150,Denver,ACTIVE
                30,OnlyInDs2A,2000,Miami,NEW
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
        completedComparisonId = uploadResponse.comparisonId();

        ComparisonExecuteRequest executeRequest = new ComparisonExecuteRequest(
                List.of("id"),
                List.of(new ToleranceConfig("amount", 1.0)),
                true
        );

        mockMvc.perform(post("/api/v1/comparisons/" + completedComparisonId + "/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(executeRequest)))
                .andExpect(status().isOk());

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            mockMvc.perform(get("/api/v1/comparisons/" + completedComparisonId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(ComparisonStatus.COMPLETED.name()));
        });
    }

    @Test
    @DisplayName("GET /api/v1/comparisons/{id}/report returns valid .xlsx download with expected sheets and headers")
    void testDownloadReportSuccess() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/comparisons/" + completedComparisonId + "/report"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"comparison-" + completedComparisonId + ".xlsx\""))
                .andReturn();

        byte[] content = result.getResponse().getContentAsByteArray();
        assertThat(content).isNotEmpty();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            assertEquals(5, workbook.getNumberOfSheets());
            assertEquals("Summary", workbook.getSheetName(0));
            assertEquals("Mismatches (DS1→DS2)", workbook.getSheetName(1));
            assertEquals("Mismatches (DS2→DS1)", workbook.getSheetName(2));
            assertEquals("Missing from DS2", workbook.getSheetName(3));
            assertEquals("Missing from DS1", workbook.getSheetName(4));

            // Summary Sheet Checks
            Sheet summary = workbook.getSheet("Summary");
            assertNotNull(summary);

            // Mismatches Sheet Checks (2 mismatches: id 10, id 11)
            Sheet mismatches = workbook.getSheet("Mismatches (DS1→DS2)");
            assertNotNull(mismatches);
            assertEquals(3, mismatches.getPhysicalNumberOfRows()); // 1 header + 2 mismatch rows

            // Check freeze pane and auto filter
            Row headerRow = mismatches.getRow(0);
            assertNotNull(headerRow);
            assertTrue(workbook.getFontAt(headerRow.getCell(0).getCellStyle().getFontIndex()).getBold());

            // Check highlighted diff cells in mismatch row
            Row mismatchRow1 = mismatches.getRow(1);
            boolean foundHighlight = false;
            for (Cell cell : mismatchRow1) {
                CellStyle style = cell.getCellStyle();
                if (style.getFillPattern() == FillPatternType.SOLID_FOREGROUND &&
                        (style.getFillForegroundColor() == IndexedColors.LIGHT_ORANGE.getIndex() ||
                         style.getFillForegroundColor() == IndexedColors.ORANGE.getIndex())) {
                    foundHighlight = true;
                    break;
                }
            }
            assertTrue(foundHighlight, "Expected differing cells in mismatches to be highlighted with orange fill");

            // Missing from DS2 (1 row: id 20)
            Sheet missingDs2 = workbook.getSheet("Missing from DS2");
            assertNotNull(missingDs2);
            assertEquals(2, missingDs2.getPhysicalNumberOfRows()); // 1 header + 1 row

            // Missing from DS1 (1 row: id 30)
            Sheet missingDs1 = workbook.getSheet("Missing from DS1");
            assertNotNull(missingDs1);
            assertEquals(2, missingDs1.getPhysicalNumberOfRows()); // 1 header + 1 row
        }
    }

    @Test
    @DisplayName("GET /api/v1/comparisons/{id}/report returns 404 when comparison not found")
    void testDownloadReportNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/comparisons/non-existent-id/report"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/comparisons/{id}/report returns 409 when comparison is not completed")
    void testDownloadReportNotCompleted() throws Exception {
        // Upload without executing -> status = UPLOADED
        MockMultipartFile file1 = new MockMultipartFile("ds1File", "d1.csv", "text/csv", "id\n1\n".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("ds2File", "d2.csv", "text/csv", "id\n1\n".getBytes());

        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/comparisons/upload")
                        .file(file1)
                        .file(file2))
                .andExpect(status().isOk())
                .andReturn();

        UploadResponse response = objectMapper.readValue(
                uploadResult.getResponse().getContentAsString(),
                UploadResponse.class
        );
        String uploadedId = response.comparisonId();

        mockMvc.perform(get("/api/v1/comparisons/" + uploadedId + "/report"))
                .andExpect(status().isConflict());
    }
}
