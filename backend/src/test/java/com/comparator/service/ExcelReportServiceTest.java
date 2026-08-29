package com.comparator.service;

import com.comparator.config.AppProperties;
import com.comparator.model.dto.ComparisonExecuteRequest;
import com.comparator.model.dto.ToleranceConfig;
import com.comparator.model.entity.ComparisonRecord;
import com.comparator.model.enums.ComparisonStatus;
import com.comparator.model.enums.DataSourceType;
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
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExcelReportServiceTest {

    @TempDir
    Path tempStorageDir;

    private DuckDbService duckDbService;
    private AppProperties appProperties;
    private ObjectMapper objectMapper;
    private ExcelReportService excelReportService;

    @BeforeEach
    void setUp() {
        duckDbService = new DuckDbService();
        appProperties = new AppProperties(
                new AppProperties.StorageProperties(tempStorageDir.toString()),
                new AppProperties.UploadProperties("500MB"),
                new AppProperties.CleanupProperties(1),
                new AppProperties.ComparisonProperties(30)
        );
        objectMapper = new ObjectMapper();
        excelReportService = new ExcelReportService(duckDbService, appProperties, objectMapper);
    }

    private void parseCsvToParquet(Path csvPath, Path parquetPath) throws Exception {
        FileParserService fileParserService = new FileParserService(new DelimiterDetector(), duckDbService);
        fileParserService.parseStreamToParquet(Files.newInputStream(csvPath), parquetPath, "auto", csvPath.getFileName().toString());
    }

    @Test
    @DisplayName("Generates valid Excel report with Summary, Mismatches, and Missing sheets")
    void testGenerateValidReport() throws Exception {
        String comparisonId = "test-comp-1";
        Path compDir = tempStorageDir.resolve(comparisonId);
        Files.createDirectories(compDir);

        // Prepare test parquet files
        Path ds1Csv = compDir.resolve("ds1.csv");
        Path ds2Csv = compDir.resolve("ds2.csv");
        Files.writeString(ds1Csv, "id,name,score\n1,Alice,90\n2,Bob,80\n3,Only1,70\n");
        Files.writeString(ds2Csv, "id,name,score\n1,Alice,90\n2,Robert,85\n4,Only2,60\n");

        parseCsvToParquet(ds1Csv, compDir.resolve("ds1.parquet"));
        parseCsvToParquet(ds2Csv, compDir.resolve("ds2.parquet"));

        ComparisonEngine engine = new ComparisonEngine(duckDbService);
        engine.compare(
                compDir.resolve("ds1.parquet"),
                compDir.resolve("ds2.parquet"),
                compDir,
                List.of("id"),
                List.of(new ToleranceConfig("score", 1.0)),
                true
        );

        ComparisonRecord record = new ComparisonRecord();
        record.setId(comparisonId);
        record.setStatus(ComparisonStatus.COMPLETED);
        record.setCreatedAt(LocalDateTime.of(2026, 8, 28, 10, 0, 0));
        record.setCompletedAt(LocalDateTime.of(2026, 8, 28, 10, 0, 5));
        record.setDs1Type(DataSourceType.FILE_UPLOAD);
        record.setDs1FileName("input1.csv");
        record.setDs2Type(DataSourceType.FILE_UPLOAD);
        record.setDs2FileName("input2.csv");
        record.setDs1RecordCount(3L);
        record.setDs2RecordCount(3L);
        record.setDs1FullyMatching(1L);
        record.setDs2FullyMatching(1L);
        record.setDs1NotMatching(1L);
        record.setDs2NotMatching(1L);
        record.setDs1MissingInDs2(1L);
        record.setDs2MissingInDs1(1L);

        ComparisonExecuteRequest req = new ComparisonExecuteRequest(
                List.of("id"),
                List.of(new ToleranceConfig("score", 1.0)),
                true
        );
        record.setConfigJson(objectMapper.writeValueAsString(req));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        excelReportService.generateReport(record, out);

        byte[] bytes = out.toByteArray();
        assertThat(bytes).isNotEmpty();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(5, workbook.getNumberOfSheets());
            assertEquals("Summary", workbook.getSheetName(0));
            assertEquals("Mismatches (DS1→DS2)", workbook.getSheetName(1));
            assertEquals("Mismatches (DS2→DS1)", workbook.getSheetName(2));
            assertEquals("Missing from DS2", workbook.getSheetName(3));
            assertEquals("Missing from DS1", workbook.getSheetName(4));

            // Verify Summary sheet content
            Sheet summarySheet = workbook.getSheetAt(0);
            assertNotNull(summarySheet);
            boolean foundId = false;
            boolean foundDs1Count = false;
            for (Row row : summarySheet) {
                Cell cell0 = row.getCell(0);
                if (cell0 != null && "Comparison ID".equals(cell0.getStringCellValue())) {
                    Cell cell1 = row.getCell(1);
                    assertEquals(comparisonId, cell1.getStringCellValue());
                    foundId = true;
                }
                if (cell0 != null && "Dataset 1 Total Records".equals(cell0.getStringCellValue())) {
                    Cell cell1 = row.getCell(1);
                    assertEquals(3.0, cell1.getNumericCellValue());
                    foundDs1Count = true;
                }
            }
            assertTrue(foundId, "Comparison ID should be present on Summary sheet");
            assertTrue(foundDs1Count, "Dataset 1 count should be present on Summary sheet");

            // Verify Mismatches (DS1→DS2) sheet
            Sheet mismatchSheet = workbook.getSheetAt(1);
            assertEquals(2, mismatchSheet.getPhysicalNumberOfRows()); // 1 header + 1 mismatch row
            Row headerRow = mismatchSheet.getRow(0);
            assertNotNull(headerRow);
            // Header should have bold font
            CellStyle headerStyle = headerRow.getCell(0).getCellStyle();
            assertNotNull(headerStyle);
            assertTrue(workbook.getFontAt(headerStyle.getFontIndex()).getBold());
            assertEquals("DS1: id", headerRow.getCell(0).getStringCellValue());
            assertEquals("DS1: name", headerRow.getCell(1).getStringCellValue());
            assertEquals("DS1: score", headerRow.getCell(2).getStringCellValue());
            assertEquals("DS2: id", headerRow.getCell(3).getStringCellValue());
            assertEquals("DS2: name", headerRow.getCell(4).getStringCellValue());
            assertEquals("DS2: score", headerRow.getCell(5).getStringCellValue());

            Row mismatchDataRow = mismatchSheet.getRow(1);
            assertNotNull(mismatchDataRow);

            // Mismatch was id=2: DS1 name=Bob score=80 vs DS2 name=Robert score=85
            // Both name and score differed, so differing cells should have orange fill
            boolean hasHighlightedCell = false;
            for (Cell cell : mismatchDataRow) {
                CellStyle cs = cell.getCellStyle();
                if (cs.getFillPattern() == FillPatternType.SOLID_FOREGROUND &&
                        (cs.getFillForegroundColor() == IndexedColors.LIGHT_ORANGE.getIndex() ||
                         cs.getFillForegroundColor() == IndexedColors.ORANGE.getIndex())) {
                    hasHighlightedCell = true;
                    break;
                }
            }
            assertTrue(hasHighlightedCell, "Differing cells should have orange fill highlight");

            // Verify Mismatches (DS2→DS1) sheet headers (DS2 columns first)
            Sheet mismatchRevSheet = workbook.getSheetAt(2);
            assertEquals(2, mismatchRevSheet.getPhysicalNumberOfRows());
            Row revHeaderRow = mismatchRevSheet.getRow(0);
            assertEquals("DS2: id", revHeaderRow.getCell(0).getStringCellValue());
            assertEquals("DS2: name", revHeaderRow.getCell(1).getStringCellValue());
            assertEquals("DS2: score", revHeaderRow.getCell(2).getStringCellValue());
            assertEquals("DS1: id", revHeaderRow.getCell(3).getStringCellValue());

            // Verify Missing from DS2 sheet
            Sheet missingDs2Sheet = workbook.getSheetAt(3);
            assertEquals(2, missingDs2Sheet.getPhysicalNumberOfRows()); // 1 header + 1 missing row

            // Verify Missing from DS1 sheet
            Sheet missingDs1Sheet = workbook.getSheetAt(4);
            assertEquals(2, missingDs1Sheet.getPhysicalNumberOfRows()); // 1 header + 1 missing row
        }
    }

    @Test
    @DisplayName("Generates report gracefully when datasets have zero mismatches or missing records")
    void testGenerateReportWithEmptyResults() throws Exception {
        String comparisonId = "test-comp-empty";
        Path compDir = tempStorageDir.resolve(comparisonId);
        Files.createDirectories(compDir);

        Path ds1Csv = compDir.resolve("ds1.csv");
        Path ds2Csv = compDir.resolve("ds2.csv");
        Files.writeString(ds1Csv, "id,name\n1,Alice\n2,Bob\n");
        Files.writeString(ds2Csv, "id,name\n1,Alice\n2,Bob\n");

        parseCsvToParquet(ds1Csv, compDir.resolve("ds1.parquet"));
        parseCsvToParquet(ds2Csv, compDir.resolve("ds2.parquet"));

        ComparisonEngine engine = new ComparisonEngine(duckDbService);
        engine.compare(
                compDir.resolve("ds1.parquet"),
                compDir.resolve("ds2.parquet"),
                compDir,
                List.of("id"),
                List.of(),
                true
        );

        ComparisonRecord record = new ComparisonRecord();
        record.setId(comparisonId);
        record.setStatus(ComparisonStatus.COMPLETED);
        record.setDs1RecordCount(2L);
        record.setDs2RecordCount(2L);
        record.setDs1FullyMatching(2L);
        record.setDs2FullyMatching(2L);
        record.setDs1NotMatching(0L);
        record.setDs2NotMatching(0L);
        record.setDs1MissingInDs2(0L);
        record.setDs2MissingInDs1(0L);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        excelReportService.generateReport(record, out);

        byte[] bytes = out.toByteArray();
        assertThat(bytes).isNotEmpty();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(5, workbook.getNumberOfSheets());

            // Mismatches and Missing sheets should have 1 header row and 0 data rows
            Sheet mismatchSheet = workbook.getSheet("Mismatches (DS1→DS2)");
            assertEquals(1, mismatchSheet.getPhysicalNumberOfRows());

            Sheet missingDs2Sheet = workbook.getSheet("Missing from DS2");
            assertEquals(1, missingDs2Sheet.getPhysicalNumberOfRows());

            Sheet missingDs1Sheet = workbook.getSheet("Missing from DS1");
            assertEquals(1, missingDs1Sheet.getPhysicalNumberOfRows());
        }
    }
}
