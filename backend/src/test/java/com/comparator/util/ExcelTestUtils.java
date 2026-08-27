package com.comparator.util;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public final class ExcelTestUtils {

    private ExcelTestUtils() {}

    public static byte[] createTestXlsx(List<String> headers, List<List<String>> rows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            int rowIdx = 0;
            if (!headers.isEmpty()) {
                Row headerRow = sheet.createRow(rowIdx++);
                for (int i = 0; i < headers.size(); i++) {
                    headerRow.createCell(i).setCellValue(headers.get(i));
                }
            }
            for (List<String> rowData : rows) {
                Row row = sheet.createRow(rowIdx++);
                for (int i = 0; i < rowData.size(); i++) {
                    String val = rowData.get(i);
                    if (!val.isEmpty()) {
                        row.createCell(i).setCellValue(val);
                    }
                }
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public static byte[] createTestXls(List<String> headers, List<List<String>> rows) throws IOException {
        try (HSSFWorkbook workbook = new HSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            int rowIdx = 0;
            if (!headers.isEmpty()) {
                Row headerRow = sheet.createRow(rowIdx++);
                for (int i = 0; i < headers.size(); i++) {
                    headerRow.createCell(i).setCellValue(headers.get(i));
                }
            }
            for (List<String> rowData : rows) {
                Row row = sheet.createRow(rowIdx++);
                for (int i = 0; i < rowData.size(); i++) {
                    String val = rowData.get(i);
                    if (!val.isEmpty()) {
                        row.createCell(i).setCellValue(val);
                    }
                }
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
