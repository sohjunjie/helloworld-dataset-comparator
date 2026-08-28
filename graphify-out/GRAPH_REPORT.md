# Graph Report - helloworld-data-comparator  (2026-08-28)

## Corpus Check
- 99 files · ~36,435 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 818 nodes · 2271 edges · 43 communities (25 shown, 18 thin omitted)
- Extraction: 86% EXTRACTED · 14% INFERRED · 0% AMBIGUOUS · INFERRED: 325 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `91b1f5c8`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- dependencies
- data-comparator-frontend
- org.junit.jupiter.api.DisplayName
- devDependencies
- Dataset Comparison Workflow
- ComparisonRecord
- App
- ComparisonService
- ColumnSelectorComponent
- ComparisonStatus
- DataComparatorApplication
- com.comparator:data-comparator-backend
- DatasetInputComponent
- WebConfig.java
- ColumnHeader
- ToleranceConfigComponent
- rxjs
- AppProperties
- FileParserService
- rules/graphify.md
- workflows/graphify.md
- ExcelReportService
- scripts
- package.json
- @angular/common
- ComparisonController.java
- DetailTableComponent
- @angular/material
- @angular/forms
- @codemirror/state
- CompareComponent
- SummaryChartComponent
- ResultsComponent
- compare.component.ts
- @angular/platform-browser
- chart.js
- SummaryCardsComponent
- @angular/compiler
- comparison.model.ts
- FileDropzoneComponent
- SqlEditorComponent
- HistoryComponent
- dataset-input.component.ts

## God Nodes (most connected - your core abstractions)
1. `ComparisonRecord` - 76 edges
2. `AppProperties` - 44 edges
3. `DuckDbService` - 37 edges
4. `ExcelReportService` - 32 edges
5. `ComparisonRepository` - 31 edges
6. `ComparisonController` - 29 edges
7. `ComparisonStatus` - 27 edges
8. `ComparisonService` - 24 edges
9. `DatabaseConnectionConfig` - 23 edges
10. `ComparisonService` - 22 edges

## Surprising Connections (you probably didn't know these)
- `App Storage and Comparison Properties` --implements--> `Parquet Large Dataset Disk Storage`  [INFERRED]
  backend/src/main/resources/application.yml → USER_REQUIREMENT.txt
- `App Navigation and Toolbar Template` --conceptually_related_to--> `Comparison Results Dashboard`  [INFERRED]
  frontend/src/app/app.html → USER_REQUIREMENT.txt
- `DataComparatorFrontend Project` --implements--> `Data Comparator System Requirements`  [INFERRED]
  frontend/README.md → USER_REQUIREMENT.txt
- `ComparisonController` --references--> `AppProperties`  [EXTRACTED]
  backend/src/main/java/com/comparator/controller/ComparisonController.java → backend/src/main/java/com/comparator/config/AppProperties.java
- `ExcelReportService` --references--> `AppProperties`  [EXTRACTED]
  backend/src/main/java/com/comparator/service/ExcelReportService.java → backend/src/main/java/com/comparator/config/AppProperties.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Fullstack Data Comparator Architecture** — user_requirement_data_comparator, backend_src_main_resources_application_config, frontend_readme_datacomparatorfrontend [INFERRED]

## Communities (43 total, 18 thin omitted)

### Community 0 - "dependencies"
Cohesion: 0.12
Nodes (17): @angular/cdk, @angular/core, @angular/router, codemirror, @codemirror/lang-sql, @codemirror/view, dependencies, @angular/cdk (+9 more)

### Community 1 - "data-comparator-frontend"
Cohesion: 0.05
Nodes (43): build, serve, test, builder, configurations, defaultConfiguration, options, cli (+35 more)

### Community 2 - "org.junit.jupiter.api.DisplayName"
Cohesion: 0.05
Nodes (12): ProgressUpdate, ToleranceConfig, ComparisonExecuteIntegrationTest, ComparisonResultsIntegrationTest, ComparisonUploadControllerTest, ComparisonEngineTest, DuckDbServiceTest, FileParserServiceTest (+4 more)

### Community 3 - "devDependencies"
Cohesion: 0.13
Nodes (15): @angular/build, @angular/cli, @angular/compiler-cli, devDependencies, @angular/build, @angular/cli, @angular/compiler-cli, jsdom (+7 more)

### Community 4 - "Dataset Comparison Workflow"
Cohesion: 0.18
Nodes (11): App Storage and Comparison Properties, Backend Application Configuration, H2 Database Configuration, DataComparatorFrontend Project, App Navigation and Toolbar Template, HTML Document Root, Comparison Results Dashboard, Data Comparator System Requirements (+3 more)

### Community 5 - "ComparisonRecord"
Cohesion: 0.07
Nodes (6): ComparisonRecord, MismatchDetail, MissingDetail, jakarta.persistence.Entity, jakarta.persistence.Table, org.springframework.scheduling.annotation.Scheduled

### Community 6 - "App"
Cohesion: 0.36
Nodes (4): App, appConfig, routes, Component

### Community 7 - "ComparisonService"
Cohesion: 0.16
Nodes (5): MissingDetail, PagedResult, ResultTableType, ComparisonService, Injectable

### Community 9 - "ComparisonStatus"
Cohesion: 0.09
Nodes (41): ComparisonExecuteRequest, Deserializer, Override, ComparisonRequest, ComparisonStatus, COMPARING, COMPLETED, CONVERTING (+33 more)

### Community 10 - "DataComparatorApplication"
Cohesion: 0.53
Nodes (4): DataComparatorApplication, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.ConfigurationPropertiesScan, org.springframework.scheduling.annotation.EnableScheduling

### Community 12 - "DatasetInputComponent"
Cohesion: 0.20
Nodes (4): DataSourceType, DatasetInputComponent, Component, ViewChild

### Community 13 - "WebConfig.java"
Cohesion: 0.43
Nodes (5): Override, WebConfig, org.springframework.context.annotation.Configuration, org.springframework.web.servlet.config.annotation.CorsRegistry, org.springframework.web.servlet.config.annotation.WebMvcConfigurer

### Community 16 - "ToleranceConfigComponent"
Cohesion: 0.26
Nodes (4): ToleranceConfig, ToleranceConfigComponent, ToleranceItem, Component

### Community 18 - "AppProperties"
Cohesion: 0.05
Nodes (45): AppProperties, CleanupProperties, ComparisonProperties, StorageProperties, UploadProperties, ComparisonResult, DatabaseConnectionConfig, ComparisonRepository (+37 more)

### Community 19 - "FileParserService"
Cohesion: 0.10
Nodes (9): DelimiterDetector, Override, ParquetRowWriter, FileParserService, DelimiterDetectorTest, SqlValidationTests, org.junit.jupiter.params.ParameterizedTest, org.junit.jupiter.params.provider.CsvSource (+1 more)

### Community 22 - "ExcelReportService"
Cohesion: 0.14
Nodes (12): ExcelReportService, MismatchDirection, DS1_TO_DS2, DS2_TO_DS1, PagedSheetWriter, ReportStyles, org.apache.poi.ss.usermodel.Cell, org.apache.poi.ss.usermodel.CellStyle (+4 more)

### Community 23 - "scripts"
Cohesion: 0.33
Nodes (6): scripts, build, ng, start, test, watch

### Community 24 - "package.json"
Cohesion: 0.40
Nodes (4): name, packageManager, private, version

### Community 26 - "ComparisonController.java"
Cohesion: 0.06
Nodes (28): ComparisonController, DatasetColumns, GlobalExceptionHandler, HealthController, HealthResponse, ComparisonSummary, DatasetColumns, MismatchDetail (+20 more)

### Community 27 - "DetailTableComponent"
Cohesion: 0.23
Nodes (3): MismatchDetail, DetailTableComponent, Component

### Community 32 - "CompareComponent"
Cohesion: 0.18
Nodes (4): CompareComponent, logOrHandleError(), Component, ViewChild

### Community 36 - "compare.component.ts"
Cohesion: 0.40
Nodes (3): ProgressUpdate, ProgressService, Injectable

### Community 46 - "comparison.model.ts"
Cohesion: 0.28
Nodes (7): ComparisonRequest, ComparisonStatus, ComparisonSummary, DatasetColumns, UploadConfigRequest, UploadDatasetOptions, UploadResponse

### Community 47 - "FileDropzoneComponent"
Cohesion: 0.18
Nodes (3): FileDropzoneComponent, Component, ViewChild

### Community 49 - "SqlEditorComponent"
Cohesion: 0.20
Nodes (3): SqlEditorComponent, Component, ViewChild

## Knowledge Gaps
- **80 isolated node(s):** `com.comparator:data-comparator-backend`, `PENDING`, `UPLOADING`, `UPLOADED`, `CONVERTING` (+75 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **18 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ComparisonRecord` connect `ComparisonRecord` to `ComparisonStatus`, `ComparisonController.java`, `AppProperties`, `ExcelReportService`?**
  _High betweenness centrality (0.054) - this node is a cross-community bridge._
- **Why does `DuckDbService` connect `AppProperties` to `org.junit.jupiter.api.DisplayName`, `ComparisonRecord`, `FileParserService`, `ExcelReportService`, `ComparisonController.java`?**
  _High betweenness centrality (0.028) - this node is a cross-community bridge._
- **Why does `AppProperties` connect `AppProperties` to `ComparisonStatus`, `ComparisonController.java`, `org.junit.jupiter.api.DisplayName`, `ExcelReportService`?**
  _High betweenness centrality (0.028) - this node is a cross-community bridge._
- **What connects `com.comparator:data-comparator-backend`, `PENDING`, `UPLOADING` to the rest of the system?**
  _80 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `dependencies` be split into smaller, more focused modules?**
  _Cohesion score 0.11764705882352941 - nodes in this community are weakly interconnected._
- **Should `data-comparator-frontend` be split into smaller, more focused modules?**
  _Cohesion score 0.04756871035940803 - nodes in this community are weakly interconnected._
- **Should `org.junit.jupiter.api.DisplayName` be split into smaller, more focused modules?**
  _Cohesion score 0.05265123226288275 - nodes in this community are weakly interconnected._