# Graph Report - helloworld-data-comparator  (2026-08-29)

## Corpus Check
- 102 files · ~37,680 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 870 nodes · 2340 edges · 46 communities (25 shown, 21 thin omitted)
- Extraction: 86% EXTRACTED · 14% INFERRED · 0% AMBIGUOUS · INFERRED: 326 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `058a9fa6`
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
- comparison.model.ts
- compare.component.ts
- @angular/platform-browser
- DataComparatorApplication
- com.comparator:data-comparator-backend
- DatasetInputComponent
- WebConfig.java
- ColumnHeader
- .compare
- ToleranceConfigComponent
- rxjs
- AppProperties
- DelimiterDetectorTest
- rules/graphify.md
- workflows/graphify.md
- ExcelReportService
- package.json
- chart.js
- codemirror
- ComparisonController.java
- DetailTableComponent
- @codemirror/lang-sql
- DuckDbService
- FileParserServiceTest
- @codemirror/state
- CompareComponent
- @codemirror/view
- ResultsComponent
- results.component.ts
- @angular/router
- tslib
- ComparisonRepository
- compilerOptions
- SummaryCardsComponent
- @angular/compiler
- org.junit.jupiter.api.BeforeEach
- tsconfig.spec.json
- SummaryChartComponent
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

## Communities (46 total, 21 thin omitted)

### Community 0 - "dependencies"
Cohesion: 0.12
Nodes (17): @angular/cdk, @angular/common, @angular/core, @angular/forms, @angular/material, @fontsource/roboto, dependencies, @angular/cdk (+9 more)

### Community 1 - "data-comparator-frontend"
Cohesion: 0.05
Nodes (43): build, serve, test, builder, configurations, defaultConfiguration, options, cli (+35 more)

### Community 2 - "org.junit.jupiter.api.DisplayName"
Cohesion: 0.09
Nodes (5): ComparisonExecuteIntegrationTest, ComparisonResultsIntegrationTest, ProgressServiceTest, org.junit.jupiter.api.DisplayName, org.junit.jupiter.api.Test

### Community 3 - "devDependencies"
Cohesion: 0.13
Nodes (15): @angular/build, @angular/cli, @angular/compiler-cli, devDependencies, @angular/build, @angular/cli, @angular/compiler-cli, jsdom (+7 more)

### Community 4 - "Dataset Comparison Workflow"
Cohesion: 0.18
Nodes (11): App Storage and Comparison Properties, Backend Application Configuration, H2 Database Configuration, DataComparatorFrontend Project, App Navigation and Toolbar Template, HTML Document Root, Comparison Results Dashboard, Data Comparator System Requirements (+3 more)

### Community 5 - "ComparisonRecord"
Cohesion: 0.06
Nodes (11): DatasetColumns, UploadConfigRequest, ComparisonRecord, ComparisonServiceTest, jakarta.persistence.Entity, jakarta.persistence.PrePersist, jakarta.persistence.Table, org.springframework.scheduling.annotation.Scheduled (+3 more)

### Community 6 - "app.config.ts"
Cohesion: 0.26
Nodes (6): App, appConfig, routes, Component, extractErrorMessage(), httpErrorInterceptor()

### Community 7 - "comparison.model.ts"
Cohesion: 0.19
Nodes (10): ComparisonRequest, ComparisonStatus, ComparisonSummary, DatasetColumns, PagedResult, UploadConfigRequest, UploadDatasetOptions, UploadResponse (+2 more)

### Community 8 - "compare.component.ts"
Cohesion: 0.14
Nodes (6): ToleranceConfig, ColumnSelectorComponent, Component, ToleranceItem, InstantErrorStateMatcher, Injectable

### Community 10 - "DataComparatorApplication"
Cohesion: 0.53
Nodes (4): DataComparatorApplication, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.ConfigurationPropertiesScan, org.springframework.scheduling.annotation.EnableScheduling

### Community 12 - "DatasetInputComponent"
Cohesion: 0.06
Nodes (12): DatabaseConnectionConfig, DataSourceType, DatasetInputComponent, DelimiterOption, Component, ViewChild, FileDropzoneComponent, Component (+4 more)

### Community 13 - "WebConfig.java"
Cohesion: 0.43
Nodes (5): Override, WebConfig, org.springframework.context.annotation.Configuration, org.springframework.web.servlet.config.annotation.CorsRegistry, org.springframework.web.servlet.config.annotation.WebMvcConfigurer

### Community 15 - ".compare"
Cohesion: 0.17
Nodes (3): ToleranceConfig, ComparisonEngineTest, DuckDbServiceTest

### Community 18 - "AppProperties"
Cohesion: 0.18
Nodes (15): AppProperties, CleanupProperties, ComparisonProperties, StorageProperties, UploadProperties, CleanupService, CleanupServiceTest, ComparisonService (+7 more)

### Community 19 - "DelimiterDetectorTest"
Cohesion: 0.22
Nodes (3): DelimiterDetector, DelimiterDetectorTest, org.junit.jupiter.params.provider.CsvSource

### Community 22 - "ExcelReportService"
Cohesion: 0.13
Nodes (12): ExcelReportService, MismatchDirection, DS1_TO_DS2, DS2_TO_DS1, PagedSheetWriter, ReportStyles, org.apache.poi.ss.usermodel.Cell, org.apache.poi.ss.usermodel.CellStyle (+4 more)

### Community 23 - "package.json"
Cohesion: 0.18
Nodes (10): name, packageManager, private, scripts, build, ng, start, test (+2 more)

### Community 26 - "ComparisonController.java"
Cohesion: 0.06
Nodes (26): ComparisonController, GlobalExceptionHandler, HealthController, HealthResponse, ComparisonSummary, DatasetColumns, MismatchDetail, MissingDetail (+18 more)

### Community 27 - "DetailTableComponent"
Cohesion: 0.16
Nodes (5): MismatchDetail, MissingDetail, DetailTableComponent, ResultTableType, Component

### Community 29 - "DuckDbService"
Cohesion: 0.05
Nodes (27): ComparisonResult, DatabaseConnectionConfig, ComparisonEngine, ComparisonService, MismatchDetail, MissingDetail, DuckDbService, Override (+19 more)

### Community 32 - "CompareComponent"
Cohesion: 0.16
Nodes (4): CompareComponent, logOrHandleError(), Component, ViewChild

### Community 36 - "results.component.ts"
Cohesion: 0.47
Nodes (3): ProgressUpdate, ProgressService, Injectable

### Community 40 - "ComparisonRepository"
Cohesion: 0.08
Nodes (48): ComparisonExecuteRequest, Deserializer, Override, ComparisonRequest, UploadResponse, ComparisonStatus, COMPARING, COMPLETED (+40 more)

### Community 41 - "compilerOptions"
Cohesion: 0.11
Nodes (18): angularCompilerOptions, enableI18nLegacyMessageIdFormat, strictInjectionParameters, strictInputAccessModifiers, compileOnSave, compilerOptions, experimentalDecorators, importHelpers (+10 more)

### Community 47 - "tsconfig.spec.json"
Cohesion: 0.22
Nodes (8): compilerOptions, types, extends, include, src/**/*.d.ts, src/**/*.spec.ts, ./tsconfig.json, vitest/globals

## Knowledge Gaps
- **103 isolated node(s):** `com.comparator:data-comparator-backend`, `PENDING`, `UPLOADING`, `UPLOADED`, `CONVERTING` (+98 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **21 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ComparisonRecord` connect `ComparisonRecord` to `ComparisonRepository`, `AppProperties`, `ExcelReportService`, `ComparisonController.java`, `DuckDbService`?**
  _High betweenness centrality (0.048) - this node is a cross-community bridge._
- **Why does `DuckDbService` connect `DuckDbService` to `ComparisonRecord`, `.compare`, `AppProperties`, `DelimiterDetectorTest`, `ExcelReportService`, `ComparisonController.java`, `FileParserServiceTest`?**
  _High betweenness centrality (0.025) - this node is a cross-community bridge._
- **Why does `AppProperties` connect `AppProperties` to `org.junit.jupiter.api.DisplayName`, `ComparisonRecord`, `ComparisonRepository`, `org.junit.jupiter.api.BeforeEach`, `ExcelReportService`, `ComparisonController.java`, `DuckDbService`?**
  _High betweenness centrality (0.025) - this node is a cross-community bridge._
- **What connects `com.comparator:data-comparator-backend`, `PENDING`, `UPLOADING` to the rest of the system?**
  _103 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `dependencies` be split into smaller, more focused modules?**
  _Cohesion score 0.11764705882352941 - nodes in this community are weakly interconnected._
- **Should `data-comparator-frontend` be split into smaller, more focused modules?**
  _Cohesion score 0.04756871035940803 - nodes in this community are weakly interconnected._
- **Should `org.junit.jupiter.api.DisplayName` be split into smaller, more focused modules?**
  _Cohesion score 0.08862745098039215 - nodes in this community are weakly interconnected._