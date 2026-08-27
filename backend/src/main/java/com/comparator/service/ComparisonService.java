package com.comparator.service;

import com.comparator.config.AppProperties;
import com.comparator.model.dto.ComparisonExecuteRequest;
import com.comparator.model.dto.ComparisonResult;
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
import java.util.concurrent.CompletableFuture;

@Service
public class ComparisonService {

    private static final Logger log = LoggerFactory.getLogger(ComparisonService.class);

    private final ComparisonRepository comparisonRepository;
    private final ComparisonEngine comparisonEngine;
    private final ProgressService progressService;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    public ComparisonService(ComparisonRepository comparisonRepository,
                             ComparisonEngine comparisonEngine,
                             ProgressService progressService,
                             AppProperties appProperties,
                             ObjectMapper objectMapper) {
        this.comparisonRepository = comparisonRepository;
        this.comparisonEngine = comparisonEngine;
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
}
