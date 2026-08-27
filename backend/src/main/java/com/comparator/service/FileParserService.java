package com.comparator.service;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileParserService {

    private final DelimiterDetector delimiterDetector;
    private final DuckDbService duckDbService;

    public FileParserService(DelimiterDetector delimiterDetector, DuckDbService duckDbService) {
        this.delimiterDetector = delimiterDetector;
        this.duckDbService = duckDbService;
    }

    /**
     * Parses a MultipartFile (CSV, TXT, XLS, or XLSX), converts it to Parquet via DuckDB, and returns detected column headers.
     */
    public List<String> parseFileToParquet(MultipartFile file, Path targetParquetPath, String delimiterPreference) throws IOException {
        String filename = file.getOriginalFilename();
        try (InputStream inputStream = file.getInputStream()) {
            return parseStreamToParquet(inputStream, targetParquetPath, delimiterPreference, filename);
        }
    }

    /**
     * Parses an InputStream containing CSV/TXT/Excel data, converts it to Parquet via DuckDB, and returns detected column headers.
     */
    public List<String> parseStreamToParquet(InputStream inputStream, Path targetParquetPath, String delimiterPreference) throws IOException {
        return parseStreamToParquet(inputStream, targetParquetPath, delimiterPreference, null);
    }

    /**
     * Parses an InputStream with optional filename hint, converts it to Parquet via DuckDB, and returns detected column headers.
     */
    public List<String> parseStreamToParquet(InputStream inputStream, Path targetParquetPath, String delimiterPreference, String filename) throws IOException {
        if (targetParquetPath.getParent() != null) {
            Files.createDirectories(targetParquetPath.getParent());
        }

        Path tempFile = Files.createTempFile("upload-file-", ".tmp");
        try {
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            if (isXlsx(filename)) {
                List<String> headers = parseXlsx(tempFile, targetParquetPath);
                if (headers.isEmpty() && !Files.exists(targetParquetPath)) {
                    Files.createFile(targetParquetPath);
                }
                return headers;
            } else if (isXls(filename)) {
                List<String> headers = parseXls(tempFile, targetParquetPath);
                if (headers.isEmpty() && !Files.exists(targetParquetPath)) {
                    Files.createFile(targetParquetPath);
                }
                return headers;
            } else {
                char delimiter;
                try (InputStream sampleStream = Files.newInputStream(tempFile)) {
                    delimiter = delimiterDetector.resolveDelimiter(delimiterPreference, sampleStream);
                }
                duckDbService.csvToParquet(tempFile, targetParquetPath, delimiter);
                return duckDbService.getColumnHeaders(targetParquetPath);
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private List<String> parseXlsx(Path xlsxPath, Path targetParquetPath) throws IOException {
        try (OPCPackage pkg = OPCPackage.open(xlsxPath.toFile())) {
            XSSFReader xssfReader = new XSSFReader(pkg);
            StylesTable styles = xssfReader.getStylesTable();
            ReadOnlySharedStringsTable sharedStrings = new ReadOnlySharedStringsTable(pkg);
            DataFormatter dataFormatter = new DataFormatter();

            XSSFReader.SheetIterator sheetIterator = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
            if (!sheetIterator.hasNext()) {
                return List.of();
            }

            List<String> extractedHeaders = new ArrayList<>();

            try (InputStream sheetStream = sheetIterator.next()) {
                InputSource sheetSource = new InputSource(sheetStream);
                XMLReader sheetParser = XMLHelper.newXMLReader();

                final DuckDbService.ParquetRowWriter[] writerHolder = new DuckDbService.ParquetRowWriter[1];
                final boolean[] headerProcessed = new boolean[1];

                XSSFSheetXMLHandler.SheetContentsHandler contentsHandler = new XSSFSheetXMLHandler.SheetContentsHandler() {
                    private List<String> currentRowValues = new ArrayList<>();

                    @Override
                    public void startRow(int rowNum) {
                        currentRowValues.clear();
                    }

                    @Override
                    public void endRow(int rowNum) {
                        if (!headerProcessed[0]) {
                            List<String> headers = sanitizeHeaders(currentRowValues);
                            if (!headers.isEmpty()) {
                                extractedHeaders.addAll(headers);
                                writerHolder[0] = duckDbService.createParquetRowWriter(targetParquetPath, headers);
                                headerProcessed[0] = true;
                            }
                        } else if (writerHolder[0] != null) {
                            writerHolder[0].writeRow(currentRowValues);
                        }
                        currentRowValues.clear();
                    }

                    @Override
                    public void cell(String cellReference, String formattedValue, org.apache.poi.xssf.usermodel.XSSFComment comment) {
                        int colIdx;
                        if (cellReference != null) {
                            colIdx = new CellReference(cellReference).getCol();
                        } else {
                            colIdx = currentRowValues.size();
                        }
                        while (currentRowValues.size() <= colIdx) {
                            currentRowValues.add("");
                        }
                        currentRowValues.set(colIdx, formattedValue != null ? formattedValue : "");
                    }

                    @Override
                    public void headerFooter(String text, boolean isHeader, String tagName) {
                    }
                };

                XSSFSheetXMLHandler handler = new XSSFSheetXMLHandler(
                        styles, null, sharedStrings, contentsHandler, dataFormatter, false);
                sheetParser.setContentHandler(handler);
                sheetParser.parse(sheetSource);

                if (writerHolder[0] != null) {
                    writerHolder[0].finish();
                    writerHolder[0].close();
                }
            } catch (Exception e) {
                throw new IOException("Failed to parse XLSX sheet: " + e.getMessage(), e);
            }

            return extractedHeaders;
        } catch (Exception e) {
            throw new IOException("Failed to open XLSX file: " + e.getMessage(), e);
        }
    }

    private List<String> parseXls(Path xlsPath, Path targetParquetPath) throws IOException {
        try (InputStream is = Files.newInputStream(xlsPath);
             HSSFWorkbook workbook = new HSSFWorkbook(is)) {
            if (workbook.getNumberOfSheets() == 0) {
                return List.of();
            }
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter dataFormatter = new DataFormatter();

            List<String> extractedHeaders = new ArrayList<>();
            DuckDbService.ParquetRowWriter writer = null;
            try {
                boolean headerProcessed = false;
                for (Row row : sheet) {
                    List<String> rowValues = new ArrayList<>();
                    short lastCell = row.getLastCellNum();
                    if (lastCell <= 0) {
                        continue;
                    }
                    for (int cn = 0; cn < lastCell; cn++) {
                        Cell cell = row.getCell(cn, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        if (cell == null) {
                            rowValues.add("");
                        } else {
                            rowValues.add(dataFormatter.formatCellValue(cell));
                        }
                    }

                    if (!headerProcessed) {
                        List<String> headers = sanitizeHeaders(rowValues);
                        if (!headers.isEmpty()) {
                            extractedHeaders.addAll(headers);
                            writer = duckDbService.createParquetRowWriter(targetParquetPath, headers);
                            headerProcessed = true;
                        }
                    } else if (writer != null) {
                        writer.writeRow(rowValues);
                    }
                }

                if (writer != null) {
                    writer.finish();
                }
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }

            return extractedHeaders;
        }
    }

    private List<String> sanitizeHeaders(List<String> rawHeaders) {
        if (rawHeaders == null || rawHeaders.isEmpty()) {
            return List.of();
        }
        int lastNonEmpty = -1;
        for (int i = 0; i < rawHeaders.size(); i++) {
            String val = rawHeaders.get(i);
            if (val != null && !val.trim().isEmpty()) {
                lastNonEmpty = i;
            }
        }
        if (lastNonEmpty == -1) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        Map<String, Integer> seenCounts = new HashMap<>();

        for (int i = 0; i <= lastNonEmpty; i++) {
            String val = (i < rawHeaders.size() && rawHeaders.get(i) != null) ? rawHeaders.get(i).trim() : "";
            if (val.isEmpty()) {
                val = "column_" + (i + 1);
            }
            int count = seenCounts.getOrDefault(val, 0);
            if (count == 0) {
                seenCounts.put(val, 1);
                result.add(val);
            } else {
                seenCounts.put(val, count + 1);
                result.add(val + "_" + count);
            }
        }
        return result;
    }

    private boolean isXlsx(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".xlsx");
    }

    private boolean isXls(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".xls");
    }
}
