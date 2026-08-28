package com.comparator.service;

import com.comparator.config.AppProperties;
import com.comparator.model.dto.ComparisonExecuteRequest;
import com.comparator.model.entity.ComparisonRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ExcelReportService {

    private static final Logger log = LoggerFactory.getLogger(ExcelReportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final int MAX_DATA_ROWS_PER_SHEET = 1_048_575; // 1,048,576 max Excel rows - 1 header row

    public enum MismatchDirection {
        DS1_TO_DS2,
        DS2_TO_DS1
    }

    private final DuckDbService duckDbService;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    public ExcelReportService(DuckDbService duckDbService,
                              AppProperties appProperties,
                              ObjectMapper objectMapper) {
        this.duckDbService = duckDbService;
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
    }

    public void generateReport(ComparisonRecord record, OutputStream outputStream) throws IOException {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            workbook.setCompressTempFiles(true);
            ReportStyles styles = createStyles(workbook);

            // 1. Summary Sheet
            buildSummarySheet(workbook, record, styles);

            // 2. Mismatches (DS1→DS2)
            buildMismatchesSheets(workbook, record, styles, MismatchDirection.DS1_TO_DS2, "Mismatches (DS1→DS2)");

            // 3. Mismatches (DS2→DS1)
            buildMismatchesSheets(workbook, record, styles, MismatchDirection.DS2_TO_DS1, "Mismatches (DS2→DS1)");

            // 4. Missing from DS2
            buildMissingSheets(workbook, record, styles, "missing_from_ds2", "ds1.parquet", "Missing from DS2");

            // 5. Missing from DS1
            buildMissingSheets(workbook, record, styles, "missing_from_ds1", "ds2.parquet", "Missing from DS1");

            workbook.write(outputStream);
            workbook.dispose();
        }
    }

    private void buildSummarySheet(SXSSFWorkbook workbook, ComparisonRecord record, ReportStyles styles) {
        SXSSFSheet sheet = workbook.createSheet("Summary");

        int rowIdx = 0;

        // Title
        Row titleRow = sheet.createRow(rowIdx++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Comparison Summary Report");
        titleCell.setCellStyle(styles.titleStyle);

        rowIdx++; // Blank row

        // Section 1: General Info
        Row sec1Row = sheet.createRow(rowIdx++);
        Cell sec1Cell = sec1Row.createCell(0);
        sec1Cell.setCellValue("GENERAL INFORMATION");
        sec1Cell.setCellStyle(styles.sectionHeaderStyle);

        addKeyValueRow(sheet, rowIdx++, "Comparison ID", record.getId() != null ? record.getId() : "", styles);
        addKeyValueRow(sheet, rowIdx++, "Status", record.getStatus() != null ? record.getStatus().name() : "", styles);
        addKeyValueRow(sheet, rowIdx++, "Created At", formatDateTime(record.getCreatedAt()), styles);
        addKeyValueRow(sheet, rowIdx++, "Completed At", formatDateTime(record.getCompletedAt()), styles);

        rowIdx++; // Blank row

        // Section 2: Dataset Configuration
        Row sec2Row = sheet.createRow(rowIdx++);
        Cell sec2Cell = sec2Row.createCell(0);
        sec2Cell.setCellValue("CONFIGURATION SNAPSHOT");
        sec2Cell.setCellStyle(styles.sectionHeaderStyle);

        String ds1Source = (record.getDs1Type() != null ? record.getDs1Type().name() : "N/A") +
                (record.getDs1FileName() != null ? " (" + record.getDs1FileName() + ")" : "");
        String ds2Source = (record.getDs2Type() != null ? record.getDs2Type().name() : "N/A") +
                (record.getDs2FileName() != null ? " (" + record.getDs2FileName() + ")" : "");

        addKeyValueRow(sheet, rowIdx++, "Dataset 1 Source", ds1Source, styles);
        addKeyValueRow(sheet, rowIdx++, "Dataset 2 Source", ds2Source, styles);

        ComparisonExecuteRequest config = parseConfig(record);
        String keyCols = (config != null && config.keyColumns() != null) ? String.join(", ", config.keyColumns()) : "N/A";
        String caseSensitive = (config != null && config.caseSensitive() != null) ? (config.caseSensitive() ? "Yes" : "No") : "Yes";
        String tolerances = "None";
        if (config != null && config.tolerances() != null && !config.tolerances().isEmpty()) {
            tolerances = config.tolerances().stream()
                    .map(t -> t.columnName() + " (" + t.percentage() + "%)")
                    .collect(Collectors.joining(", "));
        }

        addKeyValueRow(sheet, rowIdx++, "Key Columns", keyCols, styles);
        addKeyValueRow(sheet, rowIdx++, "Case Sensitive", caseSensitive, styles);
        addKeyValueRow(sheet, rowIdx++, "Tolerances", tolerances, styles);

        rowIdx++; // Blank row

        // Section 3: Summary Metrics
        Row sec3Row = sheet.createRow(rowIdx++);
        Cell sec3Cell = sec3Row.createCell(0);
        sec3Cell.setCellValue("SUMMARY METRICS");
        sec3Cell.setCellStyle(styles.sectionHeaderStyle);

        addNumericValueRow(sheet, rowIdx++, "Dataset 1 Total Records", record.getDs1RecordCount(), styles);
        addNumericValueRow(sheet, rowIdx++, "Dataset 2 Total Records", record.getDs2RecordCount(), styles);
        addNumericValueRow(sheet, rowIdx++, "Matching Records", record.getDs1FullyMatching(), styles);
        addNumericValueRow(sheet, rowIdx++, "Not Matching Records", record.getDs1NotMatching(), styles);
        addNumericValueRow(sheet, rowIdx++, "Missing from Dataset 2", record.getDs1MissingInDs2(), styles);
        addNumericValueRow(sheet, rowIdx++, "Missing from Dataset 1", record.getDs2MissingInDs1(), styles);

        sheet.setColumnWidth(0, 32 * 256);
        sheet.setColumnWidth(1, 50 * 256);
    }

    private void buildMismatchesSheets(SXSSFWorkbook workbook, ComparisonRecord record, ReportStyles styles, MismatchDirection direction, String baseSheetName) {
        Path storageDir = Path.of(appProperties.storage().path(), record.getId());
        Path ds1Parquet = storageDir.resolve("mismatches_ds1.parquet");
        Path ds2Parquet = storageDir.resolve("mismatches_ds2.parquet");

        List<String> dataCols = getDataColumns(ds1Parquet, storageDir, "ds1.parquet");
        if (dataCols.isEmpty()) {
            createEmptyDetailSheet(workbook, baseSheetName, List.of(), styles);
            return;
        }

        boolean isDs1ToDs2 = direction == MismatchDirection.DS1_TO_DS2;
        List<String> headers = new ArrayList<>();
        for (String col : dataCols) {
            headers.add((isDs1ToDs2 ? "DS1: " : "DS2: ") + col);
        }
        for (String col : dataCols) {
            headers.add((isDs1ToDs2 ? "DS2: " : "DS1: ") + col);
        }

        if (!Files.exists(ds1Parquet) || !Files.exists(ds2Parquet)) {
            createEmptyDetailSheet(workbook, baseSheetName, headers, styles);
            return;
        }

        long totalRows = duckDbService.countParquet(ds1Parquet);
        if (totalRows == 0L) {
            createEmptyDetailSheet(workbook, baseSheetName, headers, styles);
            return;
        }

        String selectDs1Cols = dataCols.stream().map(c -> "d1." + quote(c) + " AS \"ds1_" + c.replace("\"", "\"\"") + "\"").collect(Collectors.joining(", "));
        String selectDs2Cols = dataCols.stream().map(c -> "d2." + quote(c) + " AS \"ds2_" + c.replace("\"", "\"\"") + "\"").collect(Collectors.joining(", "));

        String sql;
        if (isDs1ToDs2) {
            sql = String.format("SELECT d1._diff_columns, %s, %s FROM read_parquet('%s') d1 JOIN read_parquet('%s') d2 ON d1._row_id = d2._row_id ORDER BY d1._row_id",
                    selectDs1Cols, selectDs2Cols, normalizePath(ds1Parquet), normalizePath(ds2Parquet));
        } else {
            sql = String.format("SELECT d2._diff_columns, %s, %s FROM read_parquet('%s') d2 JOIN read_parquet('%s') d1 ON d2._row_id = d1._row_id ORDER BY d2._row_id",
                    selectDs2Cols, selectDs1Cols, normalizePath(ds2Parquet), normalizePath(ds1Parquet));
        }

        PagedSheetWriter sheetWriter = new PagedSheetWriter(workbook, baseSheetName, headers, totalRows, styles);

        try (Connection conn = duckDbService.createConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                SXSSFSheet currentSheet = sheetWriter.nextDataSheet();
                Row row = currentSheet.createRow(sheetWriter.getRowsInCurrentSheet() + 1);
                String diffStr = rs.getString(1);
                Set<String> diffSet = parseDiffColumns(diffStr);

                int cellIdx = 0;
                // Left dataset columns
                for (String col : dataCols) {
                    Cell cell = row.createCell(cellIdx++);
                    String colAlias = (isDs1ToDs2 ? "ds1_" : "ds2_") + col;
                    setCellValue(cell, rs.getObject(colAlias));
                    cell.setCellStyle(diffSet.contains(col) ? styles.diffStyle : styles.normalDataStyle);
                }
                // Right dataset columns
                for (String col : dataCols) {
                    Cell cell = row.createCell(cellIdx++);
                    String colAlias = (isDs1ToDs2 ? "ds2_" : "ds1_") + col;
                    setCellValue(cell, rs.getObject(colAlias));
                    cell.setCellStyle(diffSet.contains(col) ? styles.diffStyle : styles.normalDataStyle);
                }

                sheetWriter.incrementRowCount();
            }

            sheetWriter.finish();

        } catch (SQLException e) {
            log.error("Failed to query mismatches for report generation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate mismatch report: " + e.getMessage(), e);
        }
    }

    private void buildMissingSheets(SXSSFWorkbook workbook, ComparisonRecord record, ReportStyles styles, String parquetFileName, String fallbackSchemaFile, String baseSheetName) {
        Path storageDir = Path.of(appProperties.storage().path(), record.getId());
        Path parquetPath = storageDir.resolve(parquetFileName + ".parquet");

        List<String> dataCols = getDataColumns(parquetPath, storageDir, fallbackSchemaFile);
        if (dataCols.isEmpty()) {
            createEmptyDetailSheet(workbook, baseSheetName, List.of(), styles);
            return;
        }

        if (!Files.exists(parquetPath)) {
            createEmptyDetailSheet(workbook, baseSheetName, dataCols, styles);
            return;
        }

        long totalRows = duckDbService.countParquet(parquetPath);
        if (totalRows == 0L) {
            createEmptyDetailSheet(workbook, baseSheetName, dataCols, styles);
            return;
        }

        String selectCols = dataCols.stream().map(this::quote).collect(Collectors.joining(", "));
        String sql = String.format("SELECT %s FROM read_parquet('%s')", selectCols, normalizePath(parquetPath));

        PagedSheetWriter sheetWriter = new PagedSheetWriter(workbook, baseSheetName, dataCols, totalRows, styles);

        try (Connection conn = duckDbService.createConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                SXSSFSheet currentSheet = sheetWriter.nextDataSheet();
                Row row = currentSheet.createRow(sheetWriter.getRowsInCurrentSheet() + 1);
                for (int i = 0; i < dataCols.size(); i++) {
                    Cell cell = row.createCell(i);
                    setCellValue(cell, rs.getObject(i + 1));
                    cell.setCellStyle(styles.normalDataStyle);
                }
                sheetWriter.incrementRowCount();
            }

            sheetWriter.finish();

        } catch (SQLException e) {
            log.error("Failed to query missing data for report generation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate missing records report: " + e.getMessage(), e);
        }
    }

    private void createEmptyDetailSheet(SXSSFWorkbook workbook, String sheetName, List<String> headers, ReportStyles styles) {
        SXSSFSheet sheet = workbook.createSheet(sheetName);
        sheet.createFreezePane(0, 1);
        if (!headers.isEmpty()) {
            writeHeaderRow(sheet, headers, styles);
            sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, headers.size() - 1));
        }
    }

    private void writeHeaderRow(Sheet sheet, List<String> headers, ReportStyles styles) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(styles.headerStyle);
        }
    }

    private List<String> getDataColumns(Path targetParquet, Path storageDir, String fallbackSchemaFileName) {
        if (Files.exists(targetParquet)) {
            List<String> raw = duckDbService.getColumnHeaders(targetParquet);
            return raw.stream()
                    .filter(c -> !"_row_id".equalsIgnoreCase(c) && !"_diff_columns".equalsIgnoreCase(c))
                    .toList();
        }
        Path fallbackFile = storageDir.resolve(fallbackSchemaFileName);
        if (Files.exists(fallbackFile)) {
            return duckDbService.getColumnHeaders(fallbackFile).stream()
                    .filter(c -> !"_row_id".equalsIgnoreCase(c) && !"_diff_columns".equalsIgnoreCase(c))
                    .toList();
        }
        return List.of();
    }

    private Set<String> parseDiffColumns(String diffStr) {
        if (diffStr == null || diffStr.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(diffStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private void addKeyValueRow(Sheet sheet, int rowIdx, String label, String value, ReportStyles styles) {
        Row row = sheet.createRow(rowIdx);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(styles.summaryLabelStyle);

        Cell valCell = row.createCell(1);
        valCell.setCellValue(value != null ? value : "");
        valCell.setCellStyle(styles.summaryValueStyle);
    }

    private void addNumericValueRow(Sheet sheet, int rowIdx, String label, Long value, ReportStyles styles) {
        Row row = sheet.createRow(rowIdx);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(styles.summaryLabelStyle);

        Cell valCell = row.createCell(1);
        if (value != null) {
            valCell.setCellValue(value.doubleValue());
        } else {
            valCell.setCellValue(0.0);
        }
        valCell.setCellStyle(styles.summaryValueStyle);
    }

    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof Number num) {
            cell.setCellValue(num.doubleValue());
        } else if (value instanceof Boolean bool) {
            cell.setCellValue(bool);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private String formatDateTime(LocalDateTime dt) {
        return dt != null ? dt.format(DATE_FORMATTER) : "N/A";
    }

    private ComparisonExecuteRequest parseConfig(ComparisonRecord record) {
        if (record == null || record.getConfigJson() == null || record.getConfigJson().isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(record.getConfigJson(), ComparisonExecuteRequest.class);
        } catch (Exception e) {
            log.warn("Could not parse configJson from record: {}", e.getMessage());
            return null;
        }
    }

    private String quote(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    private String normalizePath(Path path) {
        return path.toAbsolutePath().toString().replace('\\', '/').replace("'", "''");
    }

    private ReportStyles createStyles(SXSSFWorkbook workbook) {
        Font defaultFont = workbook.createFont();
        defaultFont.setFontName("Calibri");
        defaultFont.setFontHeightInPoints((short) 11);

        Font headerFont = workbook.createFont();
        headerFont.setFontName("Calibri");
        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setBold(true);

        Font titleFont = workbook.createFont();
        titleFont.setFontName("Calibri");
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setBold(true);

        Font sectionFont = workbook.createFont();
        sectionFont.setFontName("Calibri");
        sectionFont.setFontHeightInPoints((short) 11);
        sectionFont.setBold(true);

        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);

        CellStyle sectionHeaderStyle = workbook.createCellStyle();
        sectionHeaderStyle.setFont(sectionFont);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        applyBorders(headerStyle);

        CellStyle diffStyle = workbook.createCellStyle();
        diffStyle.setFont(defaultFont);
        diffStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        diffStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        applyBorders(diffStyle);

        CellStyle normalDataStyle = workbook.createCellStyle();
        normalDataStyle.setFont(defaultFont);
        applyBorders(normalDataStyle);

        CellStyle summaryLabelStyle = workbook.createCellStyle();
        summaryLabelStyle.setFont(headerFont);
        applyBorders(summaryLabelStyle);

        CellStyle summaryValueStyle = workbook.createCellStyle();
        summaryValueStyle.setFont(defaultFont);
        applyBorders(summaryValueStyle);

        return new ReportStyles(
                titleStyle,
                sectionHeaderStyle,
                headerStyle,
                diffStyle,
                normalDataStyle,
                summaryLabelStyle,
                summaryValueStyle
        );
    }

    private void applyBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private static class PagedSheetWriter {
        private final SXSSFWorkbook workbook;
        private final String baseSheetName;
        private final List<String> headers;
        private final ReportStyles styles;
        private final boolean multiSheet;
        private int sheetIndex = 1;
        private int rowsInCurrentSheet = 0;
        private SXSSFSheet currentSheet;

        public PagedSheetWriter(SXSSFWorkbook workbook, String baseSheetName, List<String> headers, long totalRows, ReportStyles styles) {
            this.workbook = workbook;
            this.baseSheetName = baseSheetName;
            this.headers = headers;
            this.styles = styles;
            this.multiSheet = totalRows > MAX_DATA_ROWS_PER_SHEET;
            initSheet();
        }

        private void initSheet() {
            String name = multiSheet ? (baseSheetName + " (" + sheetIndex + ")") : baseSheetName;
            currentSheet = workbook.createSheet(name);
            currentSheet.createFreezePane(0, 1);
            Row row = currentSheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(styles.headerStyle);
            }
            rowsInCurrentSheet = 0;
        }

        public SXSSFSheet nextDataSheet() {
            if (rowsInCurrentSheet >= MAX_DATA_ROWS_PER_SHEET) {
                currentSheet.setAutoFilter(new CellRangeAddress(0, rowsInCurrentSheet, 0, headers.size() - 1));
                sheetIndex++;
                initSheet();
            }
            return currentSheet;
        }

        public int getRowsInCurrentSheet() {
            return rowsInCurrentSheet;
        }

        public void incrementRowCount() {
            rowsInCurrentSheet++;
        }

        public void finish() {
            if (currentSheet != null && rowsInCurrentSheet >= 0) {
                currentSheet.setAutoFilter(new CellRangeAddress(0, rowsInCurrentSheet, 0, headers.size() - 1));
            }
        }
    }

    private record ReportStyles(
            CellStyle titleStyle,
            CellStyle sectionHeaderStyle,
            CellStyle headerStyle,
            CellStyle diffStyle,
            CellStyle normalDataStyle,
            CellStyle summaryLabelStyle,
            CellStyle summaryValueStyle
    ) {}
}
