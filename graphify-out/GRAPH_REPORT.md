# Graph Report - helloworld-data-comparator  (2026-08-28)

## Corpus Check
- 69 files · ~18,298 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 533 nodes · 1139 edges · 21 communities (14 shown, 7 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 122 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `56aa4545`
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
- ComparisonController.java
- comparison.model.ts
- DuckDbService
- DataComparatorApplication
- com.comparator:data-comparator-backend
- DatasetInputComponent
- WebConfig.java
- ColumnHeader
- PagedResult
- MismatchDetail.java
- MissingDetail.java
- ComparisonSqlUploadControllerTest.java
- rules/graphify.md
- workflows/graphify.md

## God Nodes (most connected - your core abstractions)
1. `ComparisonRecord` - 58 edges
2. `DatabaseConnectionConfig` - 23 edges
3. `DuckDbService` - 20 edges
4. `DatasetInputComponent` - 20 edges
5. `ComparisonStatus` - 19 edges
6. `UploadConfigRequest` - 18 edges
7. `DataSourceType` - 18 edges
8. `SqlDataSourceService` - 18 edges
9. `FileParserService` - 17 edges
10. `ComparisonController` - 16 edges

## Surprising Connections (you probably didn't know these)
- `App Storage and Comparison Properties` --implements--> `Parquet Large Dataset Disk Storage`  [INFERRED]
  backend/src/main/resources/application.yml → USER_REQUIREMENT.txt
- `App Navigation and Toolbar Template` --conceptually_related_to--> `Comparison Results Dashboard`  [INFERRED]
  frontend/src/app/app.html → USER_REQUIREMENT.txt
- `DataComparatorFrontend Project` --implements--> `Data Comparator System Requirements`  [INFERRED]
  frontend/README.md → USER_REQUIREMENT.txt
- `ComparisonController` --references--> `AppProperties`  [EXTRACTED]
  backend/src/main/java/com/comparator/controller/ComparisonController.java → backend/src/main/java/com/comparator/config/AppProperties.java
- `AppPropertiesTest` --references--> `AppProperties`  [EXTRACTED]
  backend/src/test/java/com/comparator/config/AppPropertiesTest.java → backend/src/main/java/com/comparator/config/AppProperties.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Fullstack Data Comparator Architecture** — user_requirement_data_comparator, backend_src_main_resources_application_config, frontend_readme_datacomparatorfrontend [INFERRED]

## Communities (21 total, 7 thin omitted)

### Community 0 - "dependencies"
Cohesion: 0.06
Nodes (33): @angular/cdk, @angular/common, @angular/compiler, @angular/core, @angular/forms, @angular/material, @angular/platform-browser, @angular/router (+25 more)

### Community 1 - "data-comparator-frontend"
Cohesion: 0.05
Nodes (43): build, serve, test, builder, configurations, defaultConfiguration, options, cli (+35 more)

### Community 2 - "org.junit.jupiter.api.DisplayName"
Cohesion: 0.08
Nodes (14): DatabaseConnectionConfig, AppPropertiesTest, DelimiterDetectorTest, FileParserServiceTest, ExecutionTests, SqlDataSourceServiceTest, SqlValidationTests, ExcelTestUtils (+6 more)

### Community 3 - "devDependencies"
Cohesion: 0.08
Nodes (25): @angular/build, @angular/cli, @angular/compiler-cli, devDependencies, @angular/build, @angular/cli, @angular/compiler-cli, jsdom (+17 more)

### Community 4 - "Dataset Comparison Workflow"
Cohesion: 0.18
Nodes (11): App Storage and Comparison Properties, Backend Application Configuration, H2 Database Configuration, DataComparatorFrontend Project, App Navigation and Toolbar Template, HTML Document Root, Comparison Results Dashboard, Data Comparator System Requirements (+3 more)

### Community 5 - "ComparisonRecord"
Cohesion: 0.08
Nodes (4): UploadConfigRequest, ComparisonRecord, com.fasterxml.jackson.annotation.JsonInclude, jakarta.persistence.PrePersist

### Community 6 - "App"
Cohesion: 0.33
Nodes (4): App, appConfig, routes, Component

### Community 7 - "ComparisonController.java"
Cohesion: 0.14
Nodes (13): ComparisonController, HealthController, HealthResponse, DatasetColumns, UploadResponse, org.slf4j.Logger, org.springframework.http.ResponseEntity, org.springframework.web.bind.annotation.GetMapping (+5 more)

### Community 8 - "comparison.model.ts"
Cohesion: 0.06
Nodes (25): ComparisonRequest, ComparisonStatus, ComparisonSummary, DatasetColumns, MismatchDetail, MissingDetail, PagedResult, ProgressUpdate (+17 more)

### Community 9 - "DuckDbService"
Cohesion: 0.08
Nodes (15): DelimiterDetector, DuckDbService, Override, ParquetRowWriter, FileParserService, JdbcConnectionProvider, Override, PostgresJdbcConnectionProvider (+7 more)

### Community 10 - "DataComparatorApplication"
Cohesion: 0.60
Nodes (3): DataComparatorApplication, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.ConfigurationPropertiesScan

### Community 12 - "DatasetInputComponent"
Cohesion: 0.06
Nodes (12): DatabaseConnectionConfig, DataSourceType, DatasetInputComponent, DelimiterOption, Component, ViewChild, FileDropzoneComponent, Component (+4 more)

### Community 13 - "WebConfig.java"
Cohesion: 0.43
Nodes (5): Override, WebConfig, org.springframework.context.annotation.Configuration, org.springframework.web.servlet.config.annotation.CorsRegistry, org.springframework.web.servlet.config.annotation.WebMvcConfigurer

### Community 18 - "ComparisonSqlUploadControllerTest.java"
Cohesion: 0.07
Nodes (43): AppProperties, CleanupProperties, ComparisonProperties, StorageProperties, UploadProperties, ComparisonRequest, ComparisonSummary, ToleranceConfig (+35 more)

## Knowledge Gaps
- **84 isolated node(s):** `com.comparator:data-comparator-backend`, `StorageProperties`, `UploadProperties`, `CleanupProperties`, `ComparisonProperties` (+79 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **7 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ComparisonRecord` connect `ComparisonRecord` to `ComparisonSqlUploadControllerTest.java`, `ComparisonController.java`?**
  _High betweenness centrality (0.062) - this node is a cross-community bridge._
- **Why does `FileParserService` connect `DuckDbService` to `ComparisonSqlUploadControllerTest.java`, `org.junit.jupiter.api.DisplayName`, `ComparisonController.java`?**
  _High betweenness centrality (0.025) - this node is a cross-community bridge._
- **Why does `DatabaseConnectionConfig` connect `org.junit.jupiter.api.DisplayName` to `DuckDbService`, `ComparisonSqlUploadControllerTest.java`, `ComparisonRecord`, `ComparisonController.java`?**
  _High betweenness centrality (0.019) - this node is a cross-community bridge._
- **Are the 10 inferred relationships involving `DatabaseConnectionConfig` (e.g. with `.shouldHandleConnectionErrorInUpload()` and `.shouldHandleQueryTimeoutInUpload()`) actually correct?**
  _`DatabaseConnectionConfig` has 10 INFERRED edges - model-reasoned connections that need verification._
- **What connects `com.comparator:data-comparator-backend`, `StorageProperties`, `UploadProperties` to the rest of the system?**
  _84 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `dependencies` be split into smaller, more focused modules?**
  _Cohesion score 0.06060606060606061 - nodes in this community are weakly interconnected._
- **Should `data-comparator-frontend` be split into smaller, more focused modules?**
  _Cohesion score 0.04756871035940803 - nodes in this community are weakly interconnected._