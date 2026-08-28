# Graph Report - helloworld-data-comparator  (2026-08-28)

## Corpus Check
- 92 files · ~33,665 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 771 nodes · 2060 edges · 46 communities (27 shown, 19 thin omitted)
- Extraction: 86% EXTRACTED · 14% INFERRED · 0% AMBIGUOUS · INFERRED: 286 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `23a67feb`
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
- DatabaseConnectionConfig
- ComparisonService
- .compare
- DataComparatorApplication
- com.comparator:data-comparator-backend
- DatasetInputComponent
- WebConfig.java
- ColumnHeader
- ComparisonEngine
- ToleranceConfigComponent
- rxjs
- AppProperties
- DelimiterDetectorTest
- rules/graphify.md
- workflows/graphify.md
- ExcelReportService
- scripts
- package.json
- @angular/common
- ComparisonController.java
- DetailTableComponent
- @angular/material
- ColumnSelectorComponent
- @angular/forms
- @codemirror/state
- CompareComponent
- comparison.model.ts
- FileParserServiceTest
- ResultsComponent
- compare.component.ts
- @angular/platform-browser
- chart.js
- ProgressService
- FileParserService
- DuckDbService
- JdbcConnectionProvider
- SummaryCardsComponent
- .getColumnHeaders
- @angular/compiler

## God Nodes (most connected - your core abstractions)
1. `ComparisonRecord` - 71 edges
2. `DuckDbService` - 37 edges
3. `AppProperties` - 34 edges
4. `ExcelReportService` - 32 edges
5. `ComparisonController` - 28 edges
6. `ComparisonStatus` - 26 edges
7. `DatabaseConnectionConfig` - 23 edges
8. `ComparisonRepository` - 23 edges
9. `ComparisonService` - 22 edges
10. `ProgressService` - 21 edges

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

## Communities (46 total, 19 thin omitted)

### Community 0 - "dependencies"
Cohesion: 0.12
Nodes (17): @angular/cdk, @angular/core, @angular/router, codemirror, @codemirror/lang-sql, @codemirror/view, dependencies, @angular/cdk (+9 more)

### Community 1 - "data-comparator-frontend"
Cohesion: 0.05
Nodes (43): build, serve, test, builder, configurations, defaultConfiguration, options, cli (+35 more)

### Community 2 - "org.junit.jupiter.api.DisplayName"
Cohesion: 0.10
Nodes (6): AppPropertiesTest, ComparisonExecuteIntegrationTest, ComparisonResultsIntegrationTest, ProgressServiceTest, org.junit.jupiter.api.DisplayName, org.junit.jupiter.api.Test

### Community 3 - "devDependencies"
Cohesion: 0.13
Nodes (15): @angular/build, @angular/cli, @angular/compiler-cli, devDependencies, @angular/build, @angular/cli, @angular/compiler-cli, jsdom (+7 more)

### Community 4 - "Dataset Comparison Workflow"
Cohesion: 0.18
Nodes (11): App Storage and Comparison Properties, Backend Application Configuration, H2 Database Configuration, DataComparatorFrontend Project, App Navigation and Toolbar Template, HTML Document Root, Comparison Results Dashboard, Data Comparator System Requirements (+3 more)

### Community 5 - "ComparisonRecord"
Cohesion: 0.07
Nodes (10): ComparisonRecord, ComparisonService, MismatchDetail, MissingDetail, ComparisonServiceTest, ExcelReportServiceTest, jakarta.persistence.Entity, jakarta.persistence.PrePersist (+2 more)

### Community 6 - "App"
Cohesion: 0.33
Nodes (4): App, appConfig, routes, Component

### Community 7 - "DatabaseConnectionConfig"
Cohesion: 0.17
Nodes (6): DatabaseConnectionConfig, ExecutionTests, SqlValidationTests, org.junit.jupiter.api.Nested, org.junit.jupiter.params.ParameterizedTest, org.junit.jupiter.params.provider.ValueSource

### Community 8 - "ComparisonService"
Cohesion: 0.13
Nodes (10): ComparisonRequest, DatasetColumns, MissingDetail, PagedResult, UploadConfigRequest, UploadDatasetOptions, UploadResponse, ResultTableType (+2 more)

### Community 10 - "DataComparatorApplication"
Cohesion: 0.60
Nodes (3): DataComparatorApplication, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.ConfigurationPropertiesScan

### Community 12 - "DatasetInputComponent"
Cohesion: 0.06
Nodes (12): DatabaseConnectionConfig, DataSourceType, DatasetInputComponent, DelimiterOption, Component, ViewChild, FileDropzoneComponent, Component (+4 more)

### Community 13 - "WebConfig.java"
Cohesion: 0.43
Nodes (5): Override, WebConfig, org.springframework.context.annotation.Configuration, org.springframework.web.servlet.config.annotation.CorsRegistry, org.springframework.web.servlet.config.annotation.WebMvcConfigurer

### Community 15 - "ComparisonEngine"
Cohesion: 0.21
Nodes (5): ComparisonEngine, java.sql.ResultSet, java.sql.Statement, org.slf4j.Logger, org.springframework.stereotype.Service

### Community 16 - "ToleranceConfigComponent"
Cohesion: 0.26
Nodes (4): ToleranceConfig, ToleranceConfigComponent, ToleranceItem, Component

### Community 18 - "AppProperties"
Cohesion: 0.06
Nodes (55): AppProperties, CleanupProperties, ComparisonProperties, StorageProperties, UploadProperties, ComparisonExecuteRequest, Deserializer, Override (+47 more)

### Community 22 - "ExcelReportService"
Cohesion: 0.16
Nodes (11): ExcelReportService, MismatchDirection, DS1_TO_DS2, DS2_TO_DS1, PagedSheetWriter, ReportStyles, org.apache.poi.ss.usermodel.CellStyle, org.apache.poi.ss.usermodel.Sheet (+3 more)

### Community 23 - "scripts"
Cohesion: 0.33
Nodes (6): scripts, build, ng, start, test, watch

### Community 24 - "package.json"
Cohesion: 0.40
Nodes (4): name, packageManager, private, version

### Community 26 - "ComparisonController.java"
Cohesion: 0.07
Nodes (22): ComparisonController, DatasetColumns, HealthController, HealthResponse, ComparisonRequest, ComparisonSummary, DatasetColumns, MismatchDetail (+14 more)

### Community 27 - "DetailTableComponent"
Cohesion: 0.23
Nodes (3): MismatchDetail, DetailTableComponent, Component

### Community 32 - "CompareComponent"
Cohesion: 0.18
Nodes (4): CompareComponent, logOrHandleError(), Component, ViewChild

### Community 33 - "comparison.model.ts"
Cohesion: 0.33
Nodes (4): ComparisonStatus, ComparisonSummary, SummaryChartComponent, Component

### Community 36 - "compare.component.ts"
Cohesion: 0.47
Nodes (3): ProgressUpdate, ProgressService, Injectable

### Community 39 - "ProgressService"
Cohesion: 0.24
Nodes (5): ProgressUpdate, SseEmitter, ProgressService, org.springframework.web.servlet.mvc.method.annotation.SseEmitter, ProgressUpdate

### Community 40 - "FileParserService"
Cohesion: 0.17
Nodes (4): DelimiterDetector, Override, ParquetRowWriter, FileParserService

### Community 41 - "DuckDbService"
Cohesion: 0.23
Nodes (3): DuckDbService, SqlDataSourceService, SqlDataSourceServiceTest

### Community 42 - "JdbcConnectionProvider"
Cohesion: 0.24
Nodes (6): JdbcConnectionProvider, Override, PostgresJdbcConnectionProvider, FunctionalInterface, java.sql.Connection, org.springframework.stereotype.Component

## Knowledge Gaps
- **81 isolated node(s):** `com.comparator:data-comparator-backend`, `PENDING`, `UPLOADING`, `UPLOADED`, `CONVERTING` (+76 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **19 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ComparisonRecord` connect `ComparisonRecord` to `ComparisonController.java`, `AppProperties`, `ExcelReportService`, `ComparisonEngine`?**
  _High betweenness centrality (0.053) - this node is a cross-community bridge._
- **Why does `DuckDbService` connect `DuckDbService` to `FileParserServiceTest`, `ComparisonRecord`, `FileParserService`, `.compare`, `JdbcConnectionProvider`, `.getColumnHeaders`, `ComparisonEngine`, `AppProperties`, `ExcelReportService`, `ComparisonController.java`?**
  _High betweenness centrality (0.030) - this node is a cross-community bridge._
- **Why does `AppProperties` connect `AppProperties` to `org.junit.jupiter.api.DisplayName`, `ComparisonRecord`, `ProgressService`, `DuckDbService`, `ComparisonEngine`, `ExcelReportService`, `ComparisonController.java`?**
  _High betweenness centrality (0.027) - this node is a cross-community bridge._
- **What connects `com.comparator:data-comparator-backend`, `PENDING`, `UPLOADING` to the rest of the system?**
  _81 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `dependencies` be split into smaller, more focused modules?**
  _Cohesion score 0.11764705882352941 - nodes in this community are weakly interconnected._
- **Should `data-comparator-frontend` be split into smaller, more focused modules?**
  _Cohesion score 0.04756871035940803 - nodes in this community are weakly interconnected._
- **Should `org.junit.jupiter.api.DisplayName` be split into smaller, more focused modules?**
  _Cohesion score 0.09523809523809523 - nodes in this community are weakly interconnected._