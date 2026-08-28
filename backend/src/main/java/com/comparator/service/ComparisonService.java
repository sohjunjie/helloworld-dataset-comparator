package com.comparator.service;

import com.comparator.config.AppProperties;
import com.comparator.model.dto.ComparisonExecuteRequest;
import com.comparator.model.dto.ComparisonResult;
import com.comparator.model.dto.MismatchDetail;
import com.comparator.model.dto.MissingDetail;
import com.comparator.model.dto.PagedResult;
import com.comparator.model.entity.ComparisonRecord;
import com.comparator.model.enums.ComparisonStatus;
import com.comparator.repository.ComparisonRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class ComparisonService {

    private static final Logger log = LoggerFactory.getLogger(ComparisonService.class);

    private final ComparisonRepository comparisonRepository;
    private final ComparisonEngine comparisonEngine;
    private final DuckDbService duckDbService;
    private final ProgressService progressService;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    public ComparisonService(ComparisonRepository comparisonRepository,
                             ComparisonEngine comparisonEngine,
                             DuckDbService duckDbService,
                             ProgressService progressService,
                             AppProperties appProperties,
                             ObjectMapper objectMapper) {
        this.comparisonRepository = comparisonRepository;
        this.comparisonEngine = comparisonEngine;
        this.duckDbService = duckDbService;
        this.progressService = progressService;
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
    }

    public void emitProgress(String comparisonId, String stage, int percent) {
        progressService.emit(comparisonId, stage, percent);
    }

    public void emitProgress(String comparisonId, String stage, int percent, String message) {
        progressService.emit(comparisonId, stage, percent, message);
    }

    public CompletableFuture<ComparisonRecord> executeComparisonAsync(String comparisonId, ComparisonExecuteRequest request) {
        ComparisonRecord record = comparisonRepository.findById(comparisonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comparison not found with id: " + comparisonId));

        if (request == null || request.keyColumns() == null || request.keyColumns().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one key column must be specified");
        }

        Path storageDir = Path.of(appProperties.storage().path(), comparisonId);
        Path ds1Parquet = storageDir.resolve("ds1.parquet");
        Path ds2Parquet = storageDir.resolve("ds2.parquet");

        if (!Files.exists(ds1Parquet) || !Files.exists(ds2Parquet)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Parquet dataset files not found for comparison: " + comparisonId);
        }

        try {
            record.setConfigJson(objectMapper.writeValueAsString(request));
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize comparison request config to JSON: {}", e.getMessage());
        }

        record.setStatus(ComparisonStatus.COMPARING);
        ComparisonRecord savedRecord = comparisonRepository.save(record);
        progressService.emit(comparisonId, "COMPARING", 25);

        return CompletableFuture.supplyAsync(() -> {
            try {
                progressService.emit(comparisonId, "COMPARING", 50);
                ComparisonResult result = comparisonEngine.compare(
                        ds1Parquet,
                        ds2Parquet,
                        storageDir,
                        request.keyColumns(),
                        request.tolerances(),
                        request.caseSensitive()
                );

                savedRecord.setDs1RecordCount(result.ds1RecordCount());
                savedRecord.setDs2RecordCount(result.ds2RecordCount());
                savedRecord.setDs1FullyMatching(result.ds1FullyMatching());
                savedRecord.setDs2FullyMatching(result.ds2FullyMatching());
                savedRecord.setDs1NotMatching(result.ds1NotMatching());
                savedRecord.setDs2NotMatching(result.ds2NotMatching());
                savedRecord.setDs1MissingInDs2(result.ds1MissingInDs2());
                savedRecord.setDs2MissingInDs1(result.ds2MissingInDs1());
                savedRecord.setStatus(ComparisonStatus.COMPLETED);
                savedRecord.setCompletedAt(LocalDateTime.now());
                savedRecord.setErrorMessage(null);
                ComparisonRecord finishedRecord = comparisonRepository.save(savedRecord);
                progressService.emit(comparisonId, "COMPLETED", 100);
                return finishedRecord;
            } catch (Exception e) {
                log.error("Comparison execution failed for id {}: {}", comparisonId, e.getMessage(), e);
                savedRecord.setStatus(ComparisonStatus.FAILED);
                savedRecord.setCompletedAt(LocalDateTime.now());
                savedRecord.setErrorMessage(e.getMessage());
                ComparisonRecord failedRecord = comparisonRepository.save(savedRecord);
                progressService.emit(comparisonId, "FAILED", 100, e.getMessage());
                return failedRecord;
            }
        });
    }

    public PagedResult<MismatchDetail> getMismatches(String comparisonId, int page, int size, String direction) {
        ComparisonRecord record = getCompletedComparison(comparisonId);
        List<String> keyColumns = extractKeyColumns(record);

        int sanitizedPage = Math.max(0, page);
        int sanitizedSize = sanitizeSize(size);
        int offset = sanitizedPage * sanitizedSize;

        Path storageDir = Path.of(appProperties.storage().path(), comparisonId);
        Path mismatchesDs1Path = storageDir.resolve("mismatches_ds1.parquet");
        Path mismatchesDs2Path = storageDir.resolve("mismatches_ds2.parquet");

        if (!Files.exists(mismatchesDs1Path) || !Files.exists(mismatchesDs2Path)) {
            return PagedResult.of(List.of(), sanitizedPage, sanitizedSize, 0L);
        }

        long totalElements = duckDbService.countParquet(mismatchesDs1Path);
        if (totalElements == 0L || offset >= totalElements) {
            return PagedResult.of(List.of(), sanitizedPage, sanitizedSize, totalElements);
        }

        List<Map<String, Object>> ds1Rows = duckDbService.queryParquet(mismatchesDs1Path, "_row_id", offset, sanitizedSize);
        List<Map<String, Object>> ds2Rows = duckDbService.queryParquet(mismatchesDs2Path, "_row_id", offset, sanitizedSize);

        List<MismatchDetail> details = new ArrayList<>();
        int rowCount = Math.min(ds1Rows.size(), ds2Rows.size());

        for (int i = 0; i < rowCount; i++) {
            Map<String, Object> r1 = ds1Rows.get(i);
            Map<String, Object> r2 = ds2Rows.get(i);

            Long rowNumber = (long) (offset + i + 1);
            Object rawRowId = r1.get("_row_id");
            if (rawRowId instanceof Number num) {
                rowNumber = num.longValue();
            }

            String diffStr = (String) r1.get("_diff_columns");
            List<String> differingColumns = new ArrayList<>();
            if (diffStr != null && !diffStr.isBlank()) {
                differingColumns = Arrays.stream(diffStr.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
            }

            Map<String, Object> ds1Values = new LinkedHashMap<>(r1);
            ds1Values.remove("_row_id");
            ds1Values.remove("_diff_columns");

            Map<String, Object> ds2Values = new LinkedHashMap<>(r2);
            ds2Values.remove("_row_id");
            ds2Values.remove("_diff_columns");

            Map<String, Object> keyValues = new LinkedHashMap<>();
            for (String key : keyColumns) {
                if (ds1Values.containsKey(key)) {
                    keyValues.put(key, ds1Values.get(key));
                } else if (ds2Values.containsKey(key)) {
                    keyValues.put(key, ds2Values.get(key));
                }
            }

            details.add(new MismatchDetail(rowNumber, keyValues, ds1Values, ds2Values, differingColumns));
        }

        return PagedResult.of(details, sanitizedPage, sanitizedSize, totalElements);
    }

    public PagedResult<MissingDetail> getMissing(String comparisonId, int page, int size, String direction) {
        ComparisonRecord record = getCompletedComparison(comparisonId);
        List<String> keyColumns = extractKeyColumns(record);

        int sanitizedPage = Math.max(0, page);
        int sanitizedSize = sanitizeSize(size);
        int offset = sanitizedPage * sanitizedSize;

        boolean isDs2Perspective = direction != null && direction.equalsIgnoreCase("ds2");
        String parquetFileName = isDs2Perspective ? "missing_from_ds1.parquet" : "missing_from_ds2.parquet";
        String missingFrom = isDs2Perspective ? "DS1" : "DS2";
        String dirLabel = isDs2Perspective ? "DS2" : "DS1";

        Path storageDir = Path.of(appProperties.storage().path(), comparisonId);
        Path parquetPath = storageDir.resolve(parquetFileName);

        if (!Files.exists(parquetPath)) {
            return PagedResult.of(List.of(), sanitizedPage, sanitizedSize, 0L);
        }

        long totalElements = duckDbService.countParquet(parquetPath);
        if (totalElements == 0L || offset >= totalElements) {
            return PagedResult.of(List.of(), sanitizedPage, sanitizedSize, totalElements);
        }

        List<Map<String, Object>> rows = duckDbService.queryParquet(parquetPath, offset, sanitizedSize);
        List<MissingDetail> details = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> r = rows.get(i);
            Long rowNumber = (long) (offset + i + 1);
            Object rawRowId = r.get("_row_id");
            if (rawRowId instanceof Number num) {
                rowNumber = num.longValue();
            }

            Map<String, Object> values = new LinkedHashMap<>(r);
            values.remove("_row_id");
            values.remove("_diff_columns");

            Map<String, Object> keyValues = new LinkedHashMap<>();
            for (String key : keyColumns) {
                if (values.containsKey(key)) {
                    keyValues.put(key, values.get(key));
                }
            }

            details.add(new MissingDetail(rowNumber, keyValues, values, missingFrom, dirLabel));
        }

        return PagedResult.of(details, sanitizedPage, sanitizedSize, totalElements);
    }

    public PagedResult<Map<String, Object>> getMatches(String comparisonId, int page, int size) {
        ComparisonRecord record = getCompletedComparison(comparisonId);

        int sanitizedPage = Math.max(0, page);
        int sanitizedSize = sanitizeSize(size);
        int offset = sanitizedPage * sanitizedSize;

        Path storageDir = Path.of(appProperties.storage().path(), comparisonId);
        Path parquetPath = storageDir.resolve("matches.parquet");

        if (!Files.exists(parquetPath)) {
            return PagedResult.of(List.of(), sanitizedPage, sanitizedSize, 0L);
        }

        long totalElements = duckDbService.countParquet(parquetPath);
        if (totalElements == 0L || offset >= totalElements) {
            return PagedResult.of(List.of(), sanitizedPage, sanitizedSize, totalElements);
        }

        List<Map<String, Object>> rows = duckDbService.queryParquet(parquetPath, "_row_id", offset, sanitizedSize);
        List<Map<String, Object>> cleanedRows = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Map<String, Object> cleaned = new LinkedHashMap<>(r);
            cleaned.remove("_row_id");
            cleaned.remove("_diff_columns");
            cleanedRows.add(cleaned);
        }

        return PagedResult.of(cleanedRows, sanitizedPage, sanitizedSize, totalElements);
    }

    private ComparisonRecord getCompletedComparison(String comparisonId) {
        ComparisonRecord record = comparisonRepository.findById(comparisonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comparison not found with id: " + comparisonId));
        if (record.getStatus() != ComparisonStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Comparison is not completed. Current status: " + record.getStatus());
        }
        return record;
    }

    private List<String> extractKeyColumns(ComparisonRecord record) {
        if (record.getConfigJson() == null || record.getConfigJson().isBlank()) {
            return List.of();
        }
        try {
            ComparisonExecuteRequest req = objectMapper.readValue(record.getConfigJson(), ComparisonExecuteRequest.class);
            return (req != null && req.keyColumns() != null) ? req.keyColumns() : List.of();
        } catch (Exception e) {
            log.warn("Could not deserialize keyColumns from configJson: {}", e.getMessage());
            return List.of();
        }
    }

    private int sanitizeSize(int size) {
        if (size <= 0) {
            return 50;
        }
        return Math.min(size, 1000);
    }
}
