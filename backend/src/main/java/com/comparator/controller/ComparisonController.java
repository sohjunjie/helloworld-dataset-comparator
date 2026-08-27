package com.comparator.controller;

import com.comparator.config.AppProperties;
import com.comparator.model.dto.ComparisonRequest;
import com.comparator.model.dto.ComparisonSummary;
import com.comparator.model.dto.DatasetColumns;
import com.comparator.model.dto.UploadConfigRequest;
import com.comparator.model.dto.UploadResponse;
import com.comparator.model.entity.ComparisonRecord;
import com.comparator.model.enums.ComparisonStatus;
import com.comparator.model.enums.DataSourceType;
import com.comparator.repository.ComparisonRepository;
import com.comparator.service.FileParserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/comparisons", "/api/comparisons"})
public class ComparisonController {

    private static final Logger log = LoggerFactory.getLogger(ComparisonController.class);

    private final ComparisonRepository comparisonRepository;
    private final FileParserService fileParserService;
    private final AppProperties appProperties;

    public ComparisonController(ComparisonRepository comparisonRepository,
                                FileParserService fileParserService,
                                AppProperties appProperties) {
        this.comparisonRepository = comparisonRepository;
        this.fileParserService = fileParserService;
        this.appProperties = appProperties;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> upload(
            @RequestPart("ds1File") MultipartFile ds1File,
            @RequestPart("ds2File") MultipartFile ds2File,
            @RequestPart(value = "config", required = false) UploadConfigRequest config,
            @RequestParam(value = "ds1Delimiter", required = false) String ds1DelimiterParam,
            @RequestParam(value = "ds2Delimiter", required = false) String ds2DelimiterParam
    ) {
        validateFileSize(ds1File);
        validateFileSize(ds2File);

        String comparisonId = UUID.randomUUID().toString();
        Path storageDir = Path.of(appProperties.storage().path(), comparisonId);
        Path ds1Parquet = storageDir.resolve("ds1.parquet");
        Path ds2Parquet = storageDir.resolve("ds2.parquet");

        String ds1Delim = UploadConfigRequest.resolveDs1Delimiter(config, ds1DelimiterParam);
        String ds2Delim = UploadConfigRequest.resolveDs2Delimiter(config, ds2DelimiterParam);

        List<String> ds1Columns;
        List<String> ds2Columns;
        try {
            ds1Columns = fileParserService.parseFileToParquet(ds1File, ds1Parquet, ds1Delim);
            ds2Columns = fileParserService.parseFileToParquet(ds2File, ds2Parquet, ds2Delim);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse and convert upload files: " + e.getMessage(), e);
        }

        ComparisonRecord record = new ComparisonRecord();
        record.setId(comparisonId);
        record.setStatus(ComparisonStatus.UPLOADED);
        record.setCreatedAt(LocalDateTime.now());
        record.setDs1Type(DataSourceType.FILE_UPLOAD);
        record.setDs1FileName(ds1File.getOriginalFilename());
        record.setDs2Type(DataSourceType.FILE_UPLOAD);
        record.setDs2FileName(ds2File.getOriginalFilename());

        comparisonRepository.save(record);

        UploadResponse response = new UploadResponse(comparisonId, new DatasetColumns(ds1Columns, ds2Columns));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ComparisonSummary> createComparison(@Valid @RequestBody(required = false) ComparisonRequest request) {
        ComparisonRecord record = new ComparisonRecord();
        record.setId(UUID.randomUUID().toString());
        record.setStatus(ComparisonStatus.PENDING);
        record.setCreatedAt(LocalDateTime.now());

        if (request != null) {
            record.setDs1Type(request.ds1Type());
            record.setDs1FileName(request.ds1FileName());
            record.setDs2Type(request.ds2Type());
            record.setDs2FileName(request.ds2FileName());
        }

        ComparisonRecord saved = comparisonRepository.save(record);
        return ResponseEntity.status(HttpStatus.CREATED).body(ComparisonSummary.fromEntity(saved));
    }

    @GetMapping
    public ResponseEntity<List<ComparisonSummary>> listComparisons() {
        List<ComparisonRecord> records = comparisonRepository.findAllByOrderByCreatedAtDesc();
        List<ComparisonSummary> summaries = records.stream()
                .map(ComparisonSummary::fromEntity)
                .toList();
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComparisonSummary> getComparisonById(@PathVariable String id) {
        ComparisonRecord record = comparisonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comparison not found with id: " + id));
        return ResponseEntity.ok(ComparisonSummary.fromEntity(record));
    }

    private void validateFileSize(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }
        long maxBytes;
        try {
            maxBytes = DataSize.parse(appProperties.upload().maxFileSize()).toBytes();
        } catch (IllegalArgumentException e) {
            log.error("Invalid configuration for app.upload.max-file-size: {}", appProperties.upload().maxFileSize(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server upload configuration error");
        }

        if (file.getSize() > maxBytes) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "File '" + file.getOriginalFilename() + "' exceeds maximum allowed upload size of " + appProperties.upload().maxFileSize());
        }
    }
}
