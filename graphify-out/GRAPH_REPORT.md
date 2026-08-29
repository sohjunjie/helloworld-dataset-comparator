# Graph Report - helloworld-dataset-comparator  (2026-08-29)

## Corpus Check
- 102 files · ~38,155 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 877 nodes · 2376 edges · 47 communities (29 shown, 18 thin omitted)
- Extraction: 86% EXTRACTED · 14% INFERRED · 0% AMBIGUOUS · INFERRED: 338 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `b61463af`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- dependencies
- data-comparator-frontend
- FileDropzoneComponent
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
- ToleranceConfigComponent
- rxjs
- UploadConfigRequest
- SqlEditorComponent
- rules/graphify.md
- workflows/graphify.md
- ExcelReportService
- package.json
- chart.js
- codemirror
- ComparisonController.java
- DetailTableComponent
- @codemirror/lang-sql
- AppProperties
- FileParserService
- @codemirror/state
- CompareComponent
- @codemirror/view
- ProgressService
- ResultsComponent
- compare.component.ts
- dataset-input.component.ts
- tslib
- @angular/router
- org.junit.jupiter.api.DisplayName
- compilerOptions
- SummaryCardsComponent
- @angular/compiler
- tsconfig.spec.json
- comparison.model.ts
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
- `ExcelReportService` --references--> `AppProperties`  [EXTRACTED]
  backend/src/main/java/com/comparator/service/ExcelReportService.java → backend/src/main/java/com/comparator/config/AppProperties.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Fullstack Data Comparator Architecture** — user_requirement_data_comparator, backend_src_main_resources_application_config, frontend_readme_datacomparatorfrontend [INFERRED]

## Communities (47 total, 18 thin omitted)

### Community 0 - "dependencies"
Cohesion: 0.12
Nodes (17): @angular/cdk, @angular/common, @angular/core, @angular/forms, @angular/material, @fontsource/roboto, dependencies, @angular/cdk (+9 more)

### Community 1 - "data-comparator-frontend"
Cohesion: 0.05
Nodes (43): build, serve, test, builder, configurations, defaultConfiguration, options, cli (+35 more)

### Community 2 - "FileDropzoneComponent"
Cohesion: 0.18
Nodes (3): FileDropzoneComponent, Component, ViewChild

### Community 3 - "devDependencies"
Cohesion: 0.13
Nodes (15): @angular/build, @angular/cli, @angular/compiler-cli, devDependencies, @angular/build, @angular/cli, @angular/compiler-cli, jsdom (+7 more)

### Community 4 - "Dataset Comparison Workflow"
Cohesion: 0.18
Nodes (11): App Storage and Comparison Properties, Backend Application Configuration, H2 Database Configuration, DataComparatorFrontend Project, App Navigation and Toolbar Template, HTML Document Root, Comparison Results Dashboard, Data Comparator System Requirements (+3 more)

### Community 5 - "ComparisonRecord"
Cohesion: 0.07
Nodes (9): ComparisonRecord, ComparisonRepositoryTest, ComparisonServiceTest, ExcelReportServiceTest, jakarta.persistence.Entity, jakarta.persistence.PrePersist, jakarta.persistence.Table, org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest (+1 more)

### Community 6 - "app.config.ts"
Cohesion: 0.26
Nodes (6): App, appConfig, routes, Component, extractErrorMessage(), httpErrorInterceptor()

### Community 7 - "ComparisonService"
Cohesion: 0.18
Nodes (7): ComparisonRequest, DatasetColumns, PagedResult, UploadDatasetOptions, UploadResponse, ComparisonService, Injectable

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

### Community 15 - ".compare"
Cohesion: 0.11
Nodes (4): MismatchDetail, MissingDetail, ComparisonEngineTest, DuckDbServiceTest

### Community 16 - "ToleranceConfigComponent"
Cohesion: 0.21
Nodes (4): ToleranceConfig, ToleranceConfigComponent, ToleranceItem, Component

### Community 18 - "UploadConfigRequest"
Cohesion: 0.08
Nodes (11): MismatchDetail, MissingDetail, UploadConfigRequest, ComparisonSqlUploadControllerTest, TestJdbcConfig, com.fasterxml.jackson.annotation.JsonInclude, com.fasterxml.jackson.annotation.JsonProperty, org.springframework.boot.test.context.TestConfiguration (+3 more)

### Community 19 - "SqlEditorComponent"
Cohesion: 0.20
Nodes (3): SqlEditorComponent, Component, ViewChild

### Community 22 - "ExcelReportService"
Cohesion: 0.15
Nodes (10): ExcelReportService, MismatchDirection, DS1_TO_DS2, DS2_TO_DS1, PagedSheetWriter, ReportStyles, org.apache.poi.ss.usermodel.Sheet, org.apache.poi.xssf.streaming.SXSSFSheet (+2 more)

### Community 23 - "package.json"
Cohesion: 0.18
Nodes (10): name, packageManager, private, scripts, build, ng, start, test (+2 more)

### Community 26 - "ComparisonController.java"
Cohesion: 0.10
Nodes (21): ComparisonController, DatasetColumns, GlobalExceptionHandler, HealthController, HealthResponse, ComparisonSummary, DatasetColumns, PagedResult (+13 more)

### Community 27 - "DetailTableComponent"
Cohesion: 0.17
Nodes (5): MismatchDetail, MissingDetail, DetailTableComponent, ResultTableType, Component

### Community 29 - "AppProperties"
Cohesion: 0.06
Nodes (35): AppProperties, CleanupProperties, ComparisonProperties, StorageProperties, UploadProperties, ComparisonResult, DatabaseConnectionConfig, ComparisonRepository (+27 more)

### Community 30 - "FileParserService"
Cohesion: 0.09
Nodes (8): DelimiterDetector, Override, ParquetRowWriter, FileParserService, PostgresJdbcConnectionProvider, FileParserServiceTest, ExcelTestUtils, org.springframework.stereotype.Component

### Community 32 - "CompareComponent"
Cohesion: 0.18
Nodes (4): CompareComponent, logOrHandleError(), Component, ViewChild

### Community 34 - "ProgressService"
Cohesion: 0.13
Nodes (6): ProgressUpdate, SseEmitter, ProgressService, ProgressServiceTest, org.springframework.web.servlet.mvc.method.annotation.SseEmitter, ProgressUpdate

### Community 36 - "compare.component.ts"
Cohesion: 0.47
Nodes (3): ProgressUpdate, ProgressService, Injectable

### Community 40 - "org.junit.jupiter.api.DisplayName"
Cohesion: 0.05
Nodes (49): ComparisonExecuteRequest, Deserializer, Override, ComparisonRequest, ToleranceConfig, UploadResponse, ComparisonStatus, COMPARING (+41 more)

### Community 41 - "compilerOptions"
Cohesion: 0.11
Nodes (18): angularCompilerOptions, enableI18nLegacyMessageIdFormat, strictInjectionParameters, strictInputAccessModifiers, compileOnSave, compilerOptions, experimentalDecorators, importHelpers (+10 more)

### Community 47 - "tsconfig.spec.json"
Cohesion: 0.22
Nodes (8): compilerOptions, types, extends, include, src/**/*.d.ts, src/**/*.spec.ts, ./tsconfig.json, vitest/globals

### Community 49 - "comparison.model.ts"
Cohesion: 0.24
Nodes (5): ComparisonStatus, ComparisonSummary, UploadConfigRequest, SummaryChartComponent, Component

## Knowledge Gaps
- **103 isolated node(s):** `com.comparator:data-comparator-backend`, `PENDING`, `UPLOADING`, `UPLOADED`, `CONVERTING` (+98 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **18 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ComparisonRecord` connect `ComparisonRecord` to `org.junit.jupiter.api.DisplayName`, `.compare`, `ExcelReportService`, `ComparisonController.java`, `AppProperties`?**
  _High betweenness centrality (0.047) - this node is a cross-community bridge._
- **Why does `DuckDbService` connect `AppProperties` to `ComparisonRecord`, `.compare`, `ExcelReportService`, `ComparisonController.java`, `FileParserService`?**
  _High betweenness centrality (0.025) - this node is a cross-community bridge._
- **Why does `AppProperties` connect `AppProperties` to `ProgressService`, `ComparisonRecord`, `org.junit.jupiter.api.DisplayName`, `UploadConfigRequest`, `ExcelReportService`, `ComparisonController.java`?**
  _High betweenness centrality (0.025) - this node is a cross-community bridge._
- **What connects `com.comparator:data-comparator-backend`, `PENDING`, `UPLOADING` to the rest of the system?**
  _103 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `dependencies` be split into smaller, more focused modules?**
  _Cohesion score 0.11764705882352941 - nodes in this community are weakly interconnected._
- **Should `data-comparator-frontend` be split into smaller, more focused modules?**
  _Cohesion score 0.04756871035940803 - nodes in this community are weakly interconnected._
- **Should `devDependencies` be split into smaller, more focused modules?**
  _Cohesion score 0.13333333333333333 - nodes in this community are weakly interconnected._