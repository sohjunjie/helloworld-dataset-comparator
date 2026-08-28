package com.comparator.controller;

import com.comparator.config.AppProperties;
import com.comparator.model.dto.ComparisonExecuteRequest;
import com.comparator.model.dto.ComparisonRequest;
import com.comparator.model.dto.ComparisonSummary;
import com.comparator.model.dto.DatabaseConnectionConfig;
import com.comparator.model.dto.DatasetColumns;
import com.comparator.model.dto.MismatchDetail;
import com.comparator.model.dto.MissingDetail;
import com.comparator.model.dto.PagedResult;
import com.comparator.model.dto.UploadConfigRequest;
import com.comparator.model.dto.UploadResponse;
import com.comparator.model.entity.ComparisonRecord;
import com.comparator.model.enums.ComparisonStatus;
import com.comparator.model.enums.DataSourceType;
import com.comparator.repository.ComparisonRepository;
import com.comparator.service.ComparisonService;
import com.comparator.service.DuckDbService;
import com.comparator.service.FileParserService;
import com.comparator.service.ProgressService;
import com.comparator.service.SqlDataSourceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/comparisons", "/api/comparisons"})
public class ComparisonController {

    private static final Logger log = LoggerFactory.getLogger(ComparisonController.class);

    private final ComparisonRepository comparisonRepository;
    private final FileParserService fileParserService;
    private final SqlDataSourceService sqlDataSourceService;
    private final ComparisonService comparisonService;
    private final ProgressService progressService;
    private final DuckDbService duckDbService;
    private final AppProperties appProperties;

    public ComparisonController(ComparisonRepository comparisonRepository,
                                FileParserService fileParserService,
                                SqlDataSourceService sqlDataSourceService,
                                ComparisonService comparisonService,
                                ProgressService progressService,
                                DuckDbService duckDbService,
                                AppProperties appProperties) {
        this.comparisonRepository = comparisonRepository;
        this.fileParserService = fileParserService;
        this.sqlDataSourceService = sqlDataSourceService;
        this.comparisonService = comparisonService;
        this.progressService = progressService;
        this.duckDbService = duckDbService;
        this.appProperties = appProperties;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadMultipart(
            @RequestPart(value = "ds1File", required = false) MultipartFile ds1File,
            @RequestPart(value = "ds2File", required = false) MultipartFile ds2File,
            @RequestPart(value = "config", required = false) UploadConfigRequest config,
            @RequestParam(value = "ds1Delimiter", required = false) String ds1DelimiterParam,
            @RequestParam(value = "ds2Delimiter", required = false) String ds2DelimiterParam,
            @RequestParam(value = "ds1Sql", required = false) String ds1SqlParam,
            @RequestParam(value = "ds2Sql", required = false) String ds2SqlParam
    ) {
        String comparisonId = UUID.randomUUID().toString();
        Path storageDir = Path.of(appProperties.storage().path(), comparisonId);
        Path ds1Parquet = storageDir.resolve("ds1.parquet");
        Path ds2Parquet = storageDir.resolve("ds2.parquet");

        progressService.emit(comparisonId, "UPLOADING", 10);

        String ds1Delim = UploadConfigRequest.resolveDs1Delimiter(config, ds1DelimiterParam);
        String ds2Delim = UploadConfigRequest.resolveDs2Delimiter(config, ds2DelimiterParam);

        String ds1Sql = (config != null && config.ds1Sql() != null && !config.ds1Sql().isBlank())
                ? config.ds1Sql() : ds1SqlParam;
        DatabaseConnectionConfig ds1Conn = (config != null) ? config.ds1Connection() : null;

        String ds2Sql = (config != null && config.ds2Sql() != null && !config.ds2Sql().isBlank())
                ? config.ds2Sql() : ds2SqlParam;
        DatabaseConnectionConfig ds2Conn = (config != null) ? config.ds2Connection() : null;

        ComparisonRecord record = new ComparisonRecord();
        record.setId(comparisonId);
        record.setStatus(ComparisonStatus.UPLOADED);
        record.setCreatedAt(LocalDateTime.now());

        progressService.emit(comparisonId, "CONVERTING", 30);
        List<String> ds1Columns = processDataset(1, ds1File, ds1Sql, ds1Conn, ds1Parquet, ds1Delim, record);
        progressService.emit(comparisonId, "CONVERTING", 60);
        List<String> ds2Columns = processDataset(2, ds2File, ds2Sql, ds2Conn, ds2Parquet, ds2Delim, record);

        comparisonRepository.save(record);

        UploadResponse response = new UploadResponse(comparisonId, new DatasetColumns(ds1Columns, ds2Columns));
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/upload", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UploadResponse> uploadJson(@Valid @RequestBody UploadConfigRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload configuration cannot be empty");
        }

        String comparisonId = UUID.randomUUID().toString();
        Path storageDir = Path.of(appProperties.storage().path(), comparisonId);
        Path ds1Parquet = storageDir.resolve("ds1.parquet");
        Path ds2Parquet = storageDir.resolve("ds2.parquet");

        progressService.emit(comparisonId, "UPLOADING", 10);

        ComparisonRecord record = new ComparisonRecord();
        record.setId(comparisonId);
        record.setStatus(ComparisonStatus.UPLOADED);
        record.setCreatedAt(LocalDateTime.now());

        progressService.emit(comparisonId, "CONVERTING", 30);
        List<String> ds1Columns = processDataset(1, null, request.ds1Sql(), request.ds1Connection(), ds1Parquet, "auto", record);
        progressService.emit(comparisonId, "CONVERTING", 60);
        List<String> ds2Columns = processDataset(2, null, request.ds2Sql(), request.ds2Connection(), ds2Parquet, "auto", record);

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

    @GetMapping(value = {"/{id}/events", "/{id}/progress"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeProgress(@PathVariable String id) {
        if (!comparisonRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comparison not found with id: " + id);
        }
        return progressService.subscribe(id);
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<ComparisonSummary> executeComparison(
            @PathVariable String id,
            @Valid @RequestBody ComparisonExecuteRequest request
    ) {
        ComparisonRecord record = comparisonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comparison not found with id: " + id));

        comparisonService.executeComparisonAsync(id, request);

        ComparisonRecord updated = comparisonRepository.findById(id).orElse(record);
        return ResponseEntity.ok(ComparisonSummary.fromEntity(updated));
    }

    @GetMapping("/{id}/headers")
    public ResponseEntity<DatasetColumns> getHeaders(@PathVariable String id) {
        if (!comparisonRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comparison not found with id: " + id);
        }
        Path storageDir = Path.of(appProperties.storage().path(), id);
        Path ds1Parquet = storageDir.resolve("ds1.parquet");
        Path ds2Parquet = storageDir.resolve("ds2.parquet");
        List<String> ds1Headers = Files.exists(ds1Parquet) ? duckDbService.getColumnHeaders(ds1Parquet) : List.of();
        List<String> ds2Headers = Files.exists(ds2Parquet) ? duckDbService.getColumnHeaders(ds2Parquet) : List.of();
        return ResponseEntity.ok(new DatasetColumns(ds1Headers, ds2Headers));
    }

    @GetMapping(value = {"/{id}/results/mismatches", "/{id}/mismatches"})
    public ResponseEntity<PagedResult<MismatchDetail>> getMismatches(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false, defaultValue = "ds1") String direction
    ) {
        return ResponseEntity.ok(comparisonService.getMismatches(id, page, size, direction));
    }

    @GetMapping(value = {"/{id}/results/missing", "/{id}/missing"})
    public ResponseEntity<PagedResult<MissingDetail>> getMissing(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false, defaultValue = "ds1") String direction
    ) {
        return ResponseEntity.ok(comparisonService.getMissing(id, page, size, direction));
    }

    @GetMapping(value = {"/{id}/results/matches", "/{id}/matches"})
    public ResponseEntity<PagedResult<Map<String, Object>>> getMatches(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ResponseEntity.ok(comparisonService.getMatches(id, page, size));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComparison(@PathVariable String id) {
        ComparisonRecord record = comparisonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comparison not found with id: " + id));
        Path storageDir = Path.of(appProperties.storage().path(), id);
        try {
            if (Files.exists(storageDir)) {
                FileSystemUtils.deleteRecursively(storageDir);
            }
        } catch (Exception e) {
            log.warn("Could not delete storage directory for {}: {}", id, e.getMessage());
        }
        comparisonRepository.delete(record);
        return ResponseEntity.noContent().build();
    }

    private List<String> processDataset(
            int datasetNum,
            MultipartFile file,
            String sql,
            DatabaseConnectionConfig connectionConfig,
            Path targetParquetPath,
            String delimiter,
            ComparisonRecord record
    ) {
        if (file != null && !file.isEmpty()) {
            validateFileSize(file);
            try {
                List<String> columns = fileParserService.parseFileToParquet(file, targetParquetPath, delimiter);
                if (datasetNum == 1) {
                    record.setDs1Type(DataSourceType.FILE_UPLOAD);
                    record.setDs1FileName(file.getOriginalFilename());
                } else {
                    record.setDs2Type(DataSourceType.FILE_UPLOAD);
                    record.setDs2FileName(file.getOriginalFilename());
                }
                return columns;
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to parse and convert upload file for dataset " + datasetNum + ": " + e.getMessage(), e);
            }
        } else if (sql != null && !sql.isBlank()) {
            if (connectionConfig == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Database connection configuration is required for dataset " + datasetNum + " SQL query");
            }
            List<String> columns = sqlDataSourceService.executeAndConvertToParquet(connectionConfig, sql, targetParquetPath);
            String sourceName = "PostgreSQL: " + connectionConfig.database();
            if (datasetNum == 1) {
                record.setDs1Type(DataSourceType.SQL_QUERY);
                record.setDs1FileName(sourceName);
            } else {
                record.setDs2Type(DataSourceType.SQL_QUERY);
                record.setDs2FileName(sourceName);
            }
            return columns;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Dataset " + datasetNum + " source is missing. Please provide a file or SQL query with connection configuration.");
        }
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
