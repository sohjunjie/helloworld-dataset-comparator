# Graph Report - helloworld-dataset-comparator  (2026-08-29)

## Corpus Check
- 110 files · ~38,683 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 924 nodes · 2501 edges · 59 communities (35 shown, 24 thin omitted)
- Extraction: 87% EXTRACTED · 13% INFERRED · 0% AMBIGUOUS · INFERRED: 331 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `807e69f3`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- dependencies
- data-comparator-frontend
- org.junit.jupiter.api.DisplayName
- devDependencies
- Dataset Comparison Workflow
- ComparisonRecord
- app.config.ts
- ComparisonService
- ColumnSelectorComponent
- @angular/platform-browser
- DataComparatorApplication
- com.comparator:data-comparator-backend
- DatasetInputComponent
- WebConfig.java
- ColumnHeader
- .compare
- ProgressServiceTest
- rxjs
- AppProperties
- DelimiterDetectorTest
- rules/graphify.md
- workflows/graphify.md
- ExcelReportService
- package.json
- org.junit.jupiter.api.BeforeEach
- codemirror
- ComparisonController.java
- DetailTableComponent
- @codemirror/lang-sql
- DuckDbService
- FileParserServiceTest
- @codemirror/state
- CompareComponent
- @codemirror/view
- FileParserService
- ResultsComponent
- results.component.ts
- .queryParquet
- tslib
- ComparisonService
- ComparisonStatus
- compilerOptions
- .sanitize
- SummaryCardsComponent
- DatabaseConnectionConfig
- @angular/compiler
- ProgressService
- tsconfig.spec.json
- FileDropzoneComponent
- comparison.model.ts
- HistoryComponent
- @angular/common
- SqlEditorComponent
- FileParsingStrategyTest
- SqlDataSourceService
- dataset-input.component.ts
- compare.component.ts
- SummaryChartComponent
- chart.js

## God Nodes (most connected - your core abstractions)
1. `ComparisonRecord` - 76 edges
2. `DuckDbService` - 47 edges
3. `AppProperties` - 44 edges
4. `ExcelReportService` - 32 edges
5. `ComparisonRepository` - 31 edges
6. `ComparisonController` - 29 edges
7. `ComparisonStatus` - 27 edges
8. `DatasetInputComponent` - 27 edges
9. `ComparisonService` - 24 edges
10. `DatabaseConnectionConfig` - 23 edges

## Surprising Connections (you probably didn't know these)
- `App Storage and Comparison Properties` --implements--> `Parquet Large Dataset Disk Storage`  [INFERRED]
  backend/src/main/resources/application.yml → USER_REQUIREMENT.txt
- `App Navigation and Toolbar Template` --conceptually_related_to--> `Comparison Results Dashboard`  [INFERRED]
  frontend/src/app/app.html → USER_REQUIREMENT.txt
- `DataComparatorFrontend Project` --implements--> `Data Comparator System Requirements`  [INFERRED]
  frontend/README.md → USER_REQUIREMENT.txt
- `ComparisonController` --references--> `AppProperties`  [EXTRACTED]
  backend/src/main/java/com/comparator/controller/ComparisonController.java → backend/src/main/java/com/comparator/config/AppProperties.java
- `ComparisonService` --references--> `AppProperties`  [EXTRACTED]
  backend/src/main/java/com/comparator/service/ComparisonService.java → backend/src/main/java/com/comparator/config/AppProperties.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Fullstack Data Comparator Architecture** — user_requirement_data_comparator, backend_src_main_resources_application_config, frontend_readme_datacomparatorfrontend [INFERRED]

## Communities (59 total, 24 thin omitted)

### Community 0 - "dependencies"
Cohesion: 0.12
Nodes (17): @angular/cdk, @angular/core, @angular/forms, @angular/material, @angular/router, @fontsource/roboto, dependencies, @angular/cdk (+9 more)

### Community 1 - "data-comparator-frontend"
Cohesion: 0.05
Nodes (43): build, serve, test, builder, configurations, defaultConfiguration, options, cli (+35 more)

### Community 2 - "org.junit.jupiter.api.DisplayName"
Cohesion: 0.11
Nodes (6): AppPropertiesTest, ComparisonExecuteIntegrationTest, ComparisonResultsIntegrationTest, ComparisonUploadControllerTest, org.junit.jupiter.api.DisplayName, org.junit.jupiter.api.Test

### Community 3 - "devDependencies"
Cohesion: 0.13
Nodes (15): @angular/build, @angular/cli, @angular/compiler-cli, devDependencies, @angular/build, @angular/cli, @angular/compiler-cli, jsdom (+7 more)

### Community 4 - "Dataset Comparison Workflow"
Cohesion: 0.18
Nodes (11): App Storage and Comparison Properties, Backend Application Configuration, H2 Database Configuration, DataComparatorFrontend Project, App Navigation and Toolbar Template, HTML Document Root, Comparison Results Dashboard, Data Comparator System Requirements (+3 more)

### Community 5 - "ComparisonRecord"
Cohesion: 0.08
Nodes (7): DatasetColumns, ComparisonRecord, ExcelReportServiceTest, org.springframework.scheduling.annotation.Scheduled, org.springframework.web.bind.annotation.PostMapping, org.springframework.web.multipart.MultipartFile, UploadResponse

### Community 6 - "app.config.ts"
Cohesion: 0.29
Nodes (5): App, appConfig, Component, extractErrorMessage(), httpErrorInterceptor()

### Community 7 - "ComparisonService"
Cohesion: 0.15
Nodes (5): MissingDetail, PagedResult, ResultTableType, ComparisonService, Injectable

### Community 8 - "ColumnSelectorComponent"
Cohesion: 0.16
Nodes (4): ColumnSelectorComponent, Component, InstantErrorStateMatcher, Injectable

### Community 10 - "DataComparatorApplication"
Cohesion: 0.53
Nodes (4): DataComparatorApplication, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.ConfigurationPropertiesScan, org.springframework.scheduling.annotation.EnableScheduling

### Community 12 - "DatasetInputComponent"
Cohesion: 0.16
Nodes (4): DataSourceType, DatasetInputComponent, Component, ViewChild

### Community 13 - "WebConfig.java"
Cohesion: 0.43
Nodes (5): Override, WebConfig, org.springframework.context.annotation.Configuration, org.springframework.web.servlet.config.annotation.CorsRegistry, org.springframework.web.servlet.config.annotation.WebMvcConfigurer

### Community 16 - "ProgressServiceTest"
Cohesion: 0.15
Nodes (3): ProgressUpdate, SseEmitter, ProgressServiceTest

### Community 18 - "AppProperties"
Cohesion: 0.17
Nodes (20): AppProperties, CleanupProperties, ComparisonProperties, StorageProperties, UploadProperties, ComparisonRepository, CleanupService, CleanupIntegrationTest (+12 more)

### Community 22 - "ExcelReportService"
Cohesion: 0.16
Nodes (11): ExcelReportService, MismatchDirection, DS1_TO_DS2, DS2_TO_DS1, PagedSheetWriter, ReportStyles, org.apache.poi.ss.usermodel.CellStyle, org.apache.poi.ss.usermodel.Sheet (+3 more)

### Community 23 - "package.json"
Cohesion: 0.18
Nodes (10): name, packageManager, private, scripts, build, ng, start, test (+2 more)

### Community 26 - "ComparisonController.java"
Cohesion: 0.07
Nodes (22): ComparisonController, GlobalExceptionHandler, HealthController, HealthResponse, ComparisonSummary, DatasetColumns, MismatchDetail, MissingDetail (+14 more)

### Community 27 - "DetailTableComponent"
Cohesion: 0.22
Nodes (3): MismatchDetail, DetailTableComponent, Component

### Community 29 - "DuckDbService"
Cohesion: 0.16
Nodes (11): DelimiterDetector, DuckDbService, PostgresJdbcConnectionProvider, AbstractDelimitedFileParsingStrategy, CsvFileParsingStrategy, ExcelFileParsingStrategy, TxtFileParsingStrategy, SqlDataSourceServiceTest (+3 more)

### Community 32 - "CompareComponent"
Cohesion: 0.12
Nodes (6): CompareComponent, logOrHandleError(), Component, ViewChild, ToleranceConfigComponent, Component

### Community 34 - "FileParserService"
Cohesion: 0.29
Nodes (3): FileParserService, FileParsingStrategy, org.springframework.beans.factory.annotation.Autowired

### Community 36 - "results.component.ts"
Cohesion: 0.47
Nodes (3): ProgressUpdate, ProgressService, Injectable

### Community 37 - ".queryParquet"
Cohesion: 0.12
Nodes (5): MismatchDetail, MissingDetail, Override, ParquetRowWriter, DuckDbServiceTest

### Community 39 - "ComparisonService"
Cohesion: 0.21
Nodes (7): ComparisonEngine, ComparisonService, java.sql.ResultSet, java.sql.Statement, org.slf4j.Logger, org.springframework.stereotype.Service, org.springframework.web.server.ResponseStatusException

### Community 40 - "ComparisonStatus"
Cohesion: 0.07
Nodes (44): ComparisonExecuteRequest, Deserializer, Override, ComparisonRequest, UploadResponse, ComparisonStatus, COMPARING, COMPLETED (+36 more)

### Community 41 - "compilerOptions"
Cohesion: 0.11
Nodes (18): angularCompilerOptions, enableI18nLegacyMessageIdFormat, strictInjectionParameters, strictInputAccessModifiers, compileOnSave, compilerOptions, experimentalDecorators, importHelpers (+10 more)

### Community 42 - ".sanitize"
Cohesion: 0.21
Nodes (3): Override, HeaderSanitizer, HeaderSanitizerTest

### Community 44 - "DatabaseConnectionConfig"
Cohesion: 0.18
Nodes (8): DatabaseConnectionConfig, Override, ExecutionTests, SqlValidationTests, java.sql.Connection, org.junit.jupiter.api.Nested, org.junit.jupiter.params.ParameterizedTest, org.junit.jupiter.params.provider.ValueSource

### Community 46 - "ProgressService"
Cohesion: 0.33
Nodes (3): ProgressService, org.springframework.web.servlet.mvc.method.annotation.SseEmitter, ProgressUpdate

### Community 47 - "tsconfig.spec.json"
Cohesion: 0.22
Nodes (8): compilerOptions, types, extends, include, src/**/*.d.ts, src/**/*.spec.ts, ./tsconfig.json, vitest/globals

### Community 48 - "FileDropzoneComponent"
Cohesion: 0.18
Nodes (3): FileDropzoneComponent, Component, ViewChild

### Community 49 - "comparison.model.ts"
Cohesion: 0.28
Nodes (7): ComparisonRequest, ComparisonStatus, ComparisonSummary, DatasetColumns, UploadConfigRequest, UploadDatasetOptions, UploadResponse

### Community 52 - "SqlEditorComponent"
Cohesion: 0.20
Nodes (3): SqlEditorComponent, Component, ViewChild

### Community 53 - "FileParsingStrategyTest"
Cohesion: 0.15
Nodes (4): Override, Override, Override, FileParsingStrategyTest

### Community 56 - "compare.component.ts"
Cohesion: 0.32
Nodes (3): routes, ToleranceConfig, ToleranceItem

## Knowledge Gaps
- **103 isolated node(s):** `com.comparator:data-comparator-backend`, `PENDING`, `UPLOADING`, `UPLOADED`, `CONVERTING` (+98 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **24 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ComparisonRecord` connect `ComparisonRecord` to `.queryParquet`, `ComparisonService`, `ComparisonStatus`, `AppProperties`, `ExcelReportService`, `ComparisonController.java`?**
  _High betweenness centrality (0.044) - this node is a cross-community bridge._
- **Why does `DuckDbService` connect `DuckDbService` to `FileParserService`, `.queryParquet`, `ComparisonRecord`, `ComparisonService`, `.compare`, `AppProperties`, `FileParsingStrategyTest`, `ExcelReportService`, `SqlDataSourceService`, `org.junit.jupiter.api.BeforeEach`, `ComparisonController.java`, `FileParserServiceTest`?**
  _High betweenness centrality (0.042) - this node is a cross-community bridge._
- **Why does `AppProperties` connect `AppProperties` to `FileParserService`, `org.junit.jupiter.api.DisplayName`, `ComparisonRecord`, `ComparisonService`, `ComparisonStatus`, `ProgressService`, `ProgressServiceTest`, `ExcelReportService`, `org.junit.jupiter.api.BeforeEach`, `ComparisonController.java`, `DuckDbService`?**
  _High betweenness centrality (0.023) - this node is a cross-community bridge._
- **What connects `com.comparator:data-comparator-backend`, `PENDING`, `UPLOADING` to the rest of the system?**
  _103 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `dependencies` be split into smaller, more focused modules?**
  _Cohesion score 0.11764705882352941 - nodes in this community are weakly interconnected._
- **Should `data-comparator-frontend` be split into smaller, more focused modules?**
  _Cohesion score 0.04756871035940803 - nodes in this community are weakly interconnected._
- **Should `org.junit.jupiter.api.DisplayName` be split into smaller, more focused modules?**
  _Cohesion score 0.10685249709639953 - nodes in this community are weakly interconnected._