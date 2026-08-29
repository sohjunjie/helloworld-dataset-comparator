# Graph Report - helloworld-dataset-comparator  (2026-08-29)

## Corpus Check
- 110 files · ~39,383 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 923 nodes · 2499 edges · 56 communities (35 shown, 21 thin omitted)
- Extraction: 87% EXTRACTED · 13% INFERRED · 0% AMBIGUOUS · INFERRED: 331 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `2b5ce229`
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
- MismatchDetail
- rxjs
- AppProperties
- DelimiterDetectorTest
- rules/graphify.md
- workflows/graphify.md
- ExcelReportService
- package.json
- chart.js
- codemirror
- ComparisonController
- DetailTableComponent
- @codemirror/lang-sql
- DuckDbService
- FileParserServiceTest
- @codemirror/state
- CompareComponent
- @codemirror/view
- ComparisonController.java
- ResultsComponent
- compare.component.ts
- .queryParquet
- tslib
- com.fasterxml.jackson.databind.ObjectMapper
- ComparisonStatus
- compilerOptions
- .sanitize
- ComparisonSummary
- ComparisonSqlUploadControllerTest.java
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

## Communities (56 total, 21 thin omitted)

### Community 0 - "dependencies"
Cohesion: 0.12
Nodes (17): @angular/cdk, @angular/core, @angular/forms, @angular/material, @angular/router, @fontsource/roboto, dependencies, @angular/cdk (+9 more)

### Community 1 - "data-comparator-frontend"
Cohesion: 0.05
Nodes (43): build, serve, test, builder, configurations, defaultConfiguration, options, cli (+35 more)

### Community 2 - "org.junit.jupiter.api.DisplayName"
Cohesion: 0.10
Nodes (9): SseEmitter, AppPropertiesTest, ComparisonExecuteIntegrationTest, ComparisonReportIntegrationTest, ComparisonResultsIntegrationTest, ComparisonUploadControllerTest, ProgressServiceTest, org.junit.jupiter.api.DisplayName (+1 more)

### Community 3 - "devDependencies"
Cohesion: 0.13
Nodes (15): @angular/build, @angular/cli, @angular/compiler-cli, devDependencies, @angular/build, @angular/cli, @angular/compiler-cli, jsdom (+7 more)

### Community 4 - "Dataset Comparison Workflow"
Cohesion: 0.18
Nodes (11): App Storage and Comparison Properties, Backend Application Configuration, H2 Database Configuration, DataComparatorFrontend Project, App Navigation and Toolbar Template, HTML Document Root, Comparison Results Dashboard, Data Comparator System Requirements (+3 more)

### Community 5 - "ComparisonRecord"
Cohesion: 0.11
Nodes (3): ComparisonRecord, ExcelReportServiceTest, jakarta.persistence.PrePersist

### Community 6 - "app.config.ts"
Cohesion: 0.26
Nodes (6): App, appConfig, routes, Component, extractErrorMessage(), httpErrorInterceptor()

### Community 7 - "ComparisonService"
Cohesion: 0.14
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

### Community 16 - "MismatchDetail"
Cohesion: 0.20
Nodes (4): MismatchDetail, MissingDetail, com.fasterxml.jackson.annotation.JsonInclude, com.fasterxml.jackson.annotation.JsonProperty

### Community 18 - "AppProperties"
Cohesion: 0.15
Nodes (22): AppProperties, CleanupProperties, ComparisonProperties, StorageProperties, UploadProperties, ComparisonResult, ComparisonRepository, CleanupService (+14 more)

### Community 22 - "ExcelReportService"
Cohesion: 0.16
Nodes (11): ExcelReportService, MismatchDirection, DS1_TO_DS2, DS2_TO_DS1, PagedSheetWriter, ReportStyles, org.apache.poi.ss.usermodel.CellStyle, org.apache.poi.ss.usermodel.Sheet (+3 more)

### Community 23 - "package.json"
Cohesion: 0.18
Nodes (10): name, packageManager, private, scripts, build, ng, start, test (+2 more)

### Community 26 - "ComparisonController"
Cohesion: 0.07
Nodes (23): ComparisonController, DatasetColumns, GlobalExceptionHandler, HealthController, HealthResponse, ComparisonSummary, DatasetColumns, PagedResult (+15 more)

### Community 27 - "DetailTableComponent"
Cohesion: 0.23
Nodes (3): MismatchDetail, DetailTableComponent, Component

### Community 29 - "DuckDbService"
Cohesion: 0.20
Nodes (8): DelimiterDetector, DuckDbService, PostgresJdbcConnectionProvider, AbstractDelimitedFileParsingStrategy, CsvFileParsingStrategy, TxtFileParsingStrategy, org.springframework.core.annotation.Order, org.springframework.stereotype.Component

### Community 32 - "CompareComponent"
Cohesion: 0.12
Nodes (6): CompareComponent, logOrHandleError(), Component, ViewChild, ToleranceConfigComponent, Component

### Community 34 - "ComparisonController.java"
Cohesion: 0.26
Nodes (4): FileParserService, FileParsingStrategy, jakarta.servlet.MultipartConfigElement, org.springframework.beans.factory.annotation.Autowired

### Community 36 - "compare.component.ts"
Cohesion: 0.26
Nodes (5): ProgressUpdate, ToleranceConfig, ToleranceItem, ProgressService, Injectable

### Community 37 - ".queryParquet"
Cohesion: 0.09
Nodes (7): MismatchDetail, MissingDetail, Override, ParquetRowWriter, ExcelFileParsingStrategy, Override, DuckDbServiceTest

### Community 39 - "com.fasterxml.jackson.databind.ObjectMapper"
Cohesion: 0.17
Nodes (9): ComparisonEngine, ComparisonService, com.fasterxml.jackson.databind.ObjectMapper, java.sql.ResultSet, java.sql.Statement, org.apache.poi.ss.usermodel.Cell, org.slf4j.Logger, org.springframework.stereotype.Service (+1 more)

### Community 40 - "ComparisonStatus"
Cohesion: 0.08
Nodes (31): ComparisonExecuteRequest, Deserializer, Override, ComparisonRequest, UploadResponse, ComparisonStatus, COMPARING, COMPLETED (+23 more)

### Community 41 - "compilerOptions"
Cohesion: 0.11
Nodes (18): angularCompilerOptions, enableI18nLegacyMessageIdFormat, strictInjectionParameters, strictInputAccessModifiers, compileOnSave, compilerOptions, experimentalDecorators, importHelpers (+10 more)

### Community 42 - ".sanitize"
Cohesion: 0.21
Nodes (3): Override, HeaderSanitizer, HeaderSanitizerTest

### Community 43 - "ComparisonSummary"
Cohesion: 0.13
Nodes (5): ComparisonSummary, SummaryCardsComponent, Component, SummaryChartComponent, Component

### Community 44 - "ComparisonSqlUploadControllerTest.java"
Cohesion: 0.09
Nodes (17): DatabaseConnectionConfig, UploadConfigRequest, JdbcConnectionProvider, Override, ComparisonSqlUploadControllerTest, TestJdbcConfig, ExecutionTests, SqlValidationTests (+9 more)

### Community 46 - "ProgressService"
Cohesion: 0.23
Nodes (4): ProgressUpdate, ProgressService, org.springframework.web.servlet.mvc.method.annotation.SseEmitter, ProgressUpdate

### Community 47 - "tsconfig.spec.json"
Cohesion: 0.22
Nodes (8): compilerOptions, types, extends, include, src/**/*.d.ts, src/**/*.spec.ts, ./tsconfig.json, vitest/globals

### Community 48 - "FileDropzoneComponent"
Cohesion: 0.18
Nodes (3): FileDropzoneComponent, Component, ViewChild

### Community 49 - "comparison.model.ts"
Cohesion: 0.39
Nodes (6): ComparisonRequest, ComparisonStatus, DatasetColumns, UploadConfigRequest, UploadDatasetOptions, UploadResponse

### Community 52 - "SqlEditorComponent"
Cohesion: 0.20
Nodes (3): SqlEditorComponent, Component, ViewChild

### Community 53 - "FileParsingStrategyTest"
Cohesion: 0.20
Nodes (3): Override, Override, FileParsingStrategyTest

## Knowledge Gaps
- **103 isolated node(s):** `com.comparator:data-comparator-backend`, `PENDING`, `UPLOADING`, `UPLOADED`, `CONVERTING` (+98 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **21 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ComparisonRecord` connect `ComparisonRecord` to `ComparisonController.java`, `.queryParquet`, `com.fasterxml.jackson.databind.ObjectMapper`, `ComparisonStatus`, `ComparisonSqlUploadControllerTest.java`, `AppProperties`, `ExcelReportService`, `ComparisonController`?**
  _High betweenness centrality (0.044) - this node is a cross-community bridge._
- **Why does `DuckDbService` connect `DuckDbService` to `ComparisonController.java`, `.queryParquet`, `ComparisonRecord`, `com.fasterxml.jackson.databind.ObjectMapper`, `.compare`, `AppProperties`, `FileParsingStrategyTest`, `ExcelReportService`, `SqlDataSourceService`, `ComparisonController`, `FileParserServiceTest`?**
  _High betweenness centrality (0.042) - this node is a cross-community bridge._
- **Why does `AppProperties` connect `AppProperties` to `ComparisonController.java`, `org.junit.jupiter.api.DisplayName`, `ComparisonRecord`, `com.fasterxml.jackson.databind.ObjectMapper`, `ComparisonStatus`, `ComparisonSqlUploadControllerTest.java`, `ProgressService`, `ExcelReportService`, `ComparisonController`?**
  _High betweenness centrality (0.023) - this node is a cross-community bridge._
- **What connects `com.comparator:data-comparator-backend`, `PENDING`, `UPLOADING` to the rest of the system?**
  _103 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `dependencies` be split into smaller, more focused modules?**
  _Cohesion score 0.11764705882352941 - nodes in this community are weakly interconnected._
- **Should `data-comparator-frontend` be split into smaller, more focused modules?**
  _Cohesion score 0.04756871035940803 - nodes in this community are weakly interconnected._
- **Should `org.junit.jupiter.api.DisplayName` be split into smaller, more focused modules?**
  _Cohesion score 0.10083256244218317 - nodes in this community are weakly interconnected._