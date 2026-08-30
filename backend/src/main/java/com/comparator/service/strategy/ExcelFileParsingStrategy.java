package com.comparator.service.strategy;

import com.comparator.service.DuckDbService;
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
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(30)
public class ExcelFileParsingStrategy implements FileParsingStrategy {

    private final DuckDbService duckDbService;

    public ExcelFileParsingStrategy(DuckDbService duckDbService) {
        this.duckDbService = duckDbService;
    }

    @Override
    public boolean supports(String filename) {
        if (filename == null) {
            return false;
        }
        String lower = filename.toLowerCase();
        return lower.endsWith(".xlsx") || lower.endsWith(".xls");
    }

    @Override
    public List<String> parse(Path sourceFile, Path targetParquetPath, String delimiterPreference, String originalFilename) throws IOException {
        String targetName = originalFilename;
        if (targetName == null || targetName.isBlank()) {
            targetName = sourceFile.getFileName() != null ? sourceFile.getFileName().toString() : "";
        }
        String lower = targetName.toLowerCase();
        if (lower.endsWith(".xls") && !lower.endsWith(".xlsx")) {
            return parseXls(sourceFile, targetParquetPath);
        }

        try {
            return parseXlsx(sourceFile, targetParquetPath);
        } catch (Exception xlsxEx) {
            try {
                return parseXls(sourceFile, targetParquetPath);
            } catch (Exception xlsEx) {
                throw new IOException("Failed to parse Excel file: " + xlsxEx.getMessage(), xlsxEx);
            }
        }
    }

    public List<String> parseXlsx(Path xlsxPath, Path targetParquetPath) throws IOException {
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
                            List<String> headers = HeaderSanitizer.sanitize(currentRowValues);
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
                        String cleaned = formattedValue != null ? HeaderSanitizer.stripBom(formattedValue) : "";
                        currentRowValues.set(colIdx, cleaned != null ? cleaned : "");
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

    public List<String> parseXls(Path xlsPath, Path targetParquetPath) throws IOException {
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
                            String formatted = dataFormatter.formatCellValue(cell);
                            String cleaned = formatted != null ? HeaderSanitizer.stripBom(formatted) : "";
                            rowValues.add(cleaned != null ? cleaned : "");
                        }
                    }

                    if (!headerProcessed) {
                        List<String> headers = HeaderSanitizer.sanitize(rowValues);
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
}
