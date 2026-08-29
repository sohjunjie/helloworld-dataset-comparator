# Graph Report - helloworld-dataset-comparator  (2026-08-29)

## Corpus Check
- 104 files · ~37,814 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 892 nodes · 2375 edges · 40 communities (25 shown, 15 thin omitted)
- Extraction: 86% EXTRACTED · 14% INFERRED · 0% AMBIGUOUS · INFERRED: 326 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `882e76dd`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- dependencies
- options
- org.junit.jupiter.api.DisplayName
- devDependencies
- Dataset Comparison Workflow
- ComparisonRecord
- app.config.ts
- comparison.model.ts
- compare.component.ts
- DuckDbService
- DataComparatorApplication
- com.comparator:data-comparator-backend
- DatasetInputComponent
- WebConfig.java
- ColumnHeader
- .compare
- ToleranceConfigComponent
- FileDropzoneComponent
- SqlEditorComponent
- DelimiterDetectorTest
- rules/graphify.md
- workflows/graphify.md
- ExcelReportService
- .getColumnHeaders
- dataset-input.component.ts
- ComparisonRequest
- ComparisonController.java
- DetailTableComponent
- DatabaseConnectionConfig
- FileParserServiceTest
- CompareComponent
- ResultsComponent
- results.component.ts
- AppProperties
- compilerOptions
- SummaryCardsComponent
- ProgressServiceTest
- tsconfig.spec.json
- HistoryComponent

## God Nodes (most connected - your core abstractions)
1. `ComparisonRecord` - 76 edges
2. `AppProperties` - 44 edges
3. `DuckDbService` - 37 edges
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

## Communities (40 total, 15 thin omitted)

### Community 0 - "dependencies"
Cohesion: 0.05
Nodes (43): @angular/animations, @angular/cdk, @angular/common, @angular/compiler, @angular/core, @angular/forms, @angular/material, @angular/platform-browser (+35 more)

### Community 1 - "options"
Cohesion: 0.05
Nodes (45): build, serve, builder, configurations, defaultConfiguration, options, cli, packageManager (+37 more)

### Community 2 - "org.junit.jupiter.api.DisplayName"
Cohesion: 0.10
Nodes (4): ComparisonExecuteIntegrationTest, ComparisonResultsIntegrationTest, org.junit.jupiter.api.DisplayName, org.junit.jupiter.api.Test

### Community 3 - "devDependencies"
Cohesion: 0.06
Nodes (33): @analogjs/vite-plugin-angular, @angular/build, @angular/cli, @angular/compiler-cli, @angular-devkit/build-angular, devDependencies, @analogjs/vite-plugin-angular, @angular/build (+25 more)

### Community 4 - "Dataset Comparison Workflow"
Cohesion: 0.18
Nodes (11): App Storage and Comparison Properties, Backend Application Configuration, H2 Database Configuration, DataComparatorFrontend Project, App Navigation and Toolbar Template, HTML Document Root, Comparison Results Dashboard, Data Comparator System Requirements (+3 more)

### Community 5 - "ComparisonRecord"
Cohesion: 0.07
Nodes (11): DatasetColumns, UploadConfigRequest, ComparisonRecord, ExcelReportServiceTest, jakarta.persistence.Entity, jakarta.persistence.PrePersist, jakarta.persistence.Table, org.springframework.scheduling.annotation.Scheduled (+3 more)

### Community 6 - "app.config.ts"
Cohesion: 0.24
Nodes (6): App, appConfig, routes, Component, extractErrorMessage(), httpErrorInterceptor()

### Community 7 - "comparison.model.ts"
Cohesion: 0.15
Nodes (12): ComparisonRequest, ComparisonStatus, ComparisonSummary, DatasetColumns, MissingDetail, PagedResult, UploadConfigRequest, UploadDatasetOptions (+4 more)

### Community 8 - "compare.component.ts"
Cohesion: 0.15
Nodes (6): ToleranceConfig, ColumnSelectorComponent, Component, ToleranceItem, InstantErrorStateMatcher, Injectable

### Community 9 - "DuckDbService"
Cohesion: 0.09
Nodes (10): ComparisonEngine, ComparisonService, MismatchDetail, MissingDetail, DelimiterDetector, DuckDbService, Override, ParquetRowWriter (+2 more)

### Community 10 - "DataComparatorApplication"
Cohesion: 0.53
Nodes (4): DataComparatorApplication, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.ConfigurationPropertiesScan, org.springframework.scheduling.annotation.EnableScheduling

### Community 12 - "DatasetInputComponent"
Cohesion: 0.16
Nodes (4): DataSourceType, DatasetInputComponent, Component, ViewChild

### Community 13 - "WebConfig.java"
Cohesion: 0.43
Nodes (5): Override, WebConfig, org.springframework.context.annotation.Configuration, org.springframework.web.servlet.config.annotation.CorsRegistry, org.springframework.web.servlet.config.annotation.WebMvcConfigurer

### Community 17 - "FileDropzoneComponent"
Cohesion: 0.18
Nodes (3): FileDropzoneComponent, Component, ViewChild

### Community 18 - "SqlEditorComponent"
Cohesion: 0.20
Nodes (3): SqlEditorComponent, Component, ViewChild

### Community 22 - "ExcelReportService"
Cohesion: 0.14
Nodes (13): ExcelReportService, MismatchDirection, DS1_TO_DS2, DS2_TO_DS1, PagedSheetWriter, ReportStyles, org.apache.poi.ss.usermodel.Cell, org.apache.poi.ss.usermodel.CellStyle (+5 more)

### Community 26 - "ComparisonController.java"
Cohesion: 0.06
Nodes (27): ComparisonController, GlobalExceptionHandler, HealthController, HealthResponse, ComparisonSummary, DatasetColumns, MismatchDetail, MissingDetail (+19 more)

### Community 27 - "DetailTableComponent"
Cohesion: 0.19
Nodes (3): MismatchDetail, DetailTableComponent, Component

### Community 29 - "DatabaseConnectionConfig"
Cohesion: 0.10
Nodes (15): DatabaseConnectionConfig, JdbcConnectionProvider, Override, PostgresJdbcConnectionProvider, SqlDataSourceService, ExecutionTests, SqlDataSourceServiceTest, SqlValidationTests (+7 more)

### Community 32 - "CompareComponent"
Cohesion: 0.16
Nodes (4): CompareComponent, logOrHandleError(), Component, ViewChild

### Community 36 - "results.component.ts"
Cohesion: 0.24
Nodes (5): ProgressUpdate, SummaryChartComponent, Component, ProgressService, Injectable

### Community 40 - "AppProperties"
Cohesion: 0.06
Nodes (63): AppProperties, CleanupProperties, ComparisonProperties, StorageProperties, UploadProperties, ComparisonExecuteRequest, Deserializer, Override (+55 more)

### Community 41 - "compilerOptions"
Cohesion: 0.09
Nodes (22): angularCompilerOptions, enableI18nLegacyMessageIdFormat, strictInjectionParameters, strictInputAccessModifiers, compileOnSave, compilerOptions, experimentalDecorators, importHelpers (+14 more)

### Community 47 - "tsconfig.spec.json"
Cohesion: 0.22
Nodes (8): compilerOptions, types, extends, include, src/**/*.d.ts, src/**/*.spec.ts, ./tsconfig.json, vitest/globals

## Knowledge Gaps
- **116 isolated node(s):** `com.comparator:data-comparator-backend`, `PENDING`, `UPLOADING`, `UPLOADED`, `CONVERTING` (+111 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **15 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ComparisonRecord` connect `ComparisonRecord` to `AppProperties`, `DuckDbService`, `ComparisonController.java`, `ExcelReportService`?**
  _High betweenness centrality (0.045) - this node is a cross-community bridge._
- **Why does `DuckDbService` connect `DuckDbService` to `ComparisonRecord`, `AppProperties`, `.compare`, `ExcelReportService`, `.getColumnHeaders`, `ComparisonController.java`, `DatabaseConnectionConfig`, `FileParserServiceTest`?**
  _High betweenness centrality (0.024) - this node is a cross-community bridge._
- **Why does `AppProperties` connect `AppProperties` to `org.junit.jupiter.api.DisplayName`, `ComparisonRecord`, `DuckDbService`, `ProgressServiceTest`, `ExcelReportService`, `ComparisonController.java`?**
  _High betweenness centrality (0.024) - this node is a cross-community bridge._
- **What connects `com.comparator:data-comparator-backend`, `PENDING`, `UPLOADING` to the rest of the system?**
  _116 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `dependencies` be split into smaller, more focused modules?**
  _Cohesion score 0.046511627906976744 - nodes in this community are weakly interconnected._
- **Should `options` be split into smaller, more focused modules?**
  _Cohesion score 0.04541062801932367 - nodes in this community are weakly interconnected._
- **Should `org.junit.jupiter.api.DisplayName` be split into smaller, more focused modules?**
  _Cohesion score 0.10336817653890824 - nodes in this community are weakly interconnected._