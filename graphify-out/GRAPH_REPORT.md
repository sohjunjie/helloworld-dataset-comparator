# Graph Report - helloworld-data-comparator  (2026-08-28)

## Corpus Check
- 67 files · ~16,453 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 508 nodes · 1095 edges · 22 communities (15 shown, 7 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 122 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `79f78ff0`
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
- FileDropzoneComponent
- WebConfig.java
- ColumnHeader
- PagedResult
- MismatchDetail.java
- MissingDetail.java
- ComparisonSqlUploadControllerTest.java
- DatabaseConnectionConfig
- rules/graphify.md
- workflows/graphify.md

## God Nodes (most connected - your core abstractions)
1. `ComparisonRecord` - 58 edges
2. `DatabaseConnectionConfig` - 23 edges
3. `DuckDbService` - 20 edges
4. `ComparisonStatus` - 19 edges
5. `UploadConfigRequest` - 18 edges
6. `DataSourceType` - 18 edges
7. `SqlDataSourceService` - 18 edges
8. `FileParserService` - 17 edges
9. `ComparisonController` - 16 edges
10. `ComparisonRepository` - 16 edges

## Surprising Connections (you probably didn't know these)
- `App Storage and Comparison Properties` --implements--> `Parquet Large Dataset Disk Storage`  [INFERRED]
  backend/src/main/resources/application.yml → USER_REQUIREMENT.txt
- `App Navigation and Toolbar Template` --conceptually_related_to--> `Comparison Results Dashboard`  [INFERRED]
  frontend/src/app/app.html → USER_REQUIREMENT.txt
- `DataComparatorFrontend Project` --implements--> `Data Comparator System Requirements`  [INFERRED]
  frontend/README.md → USER_REQUIREMENT.txt
- `ComparisonController` --references--> `AppProperties`  [EXTRACTED]
  backend/src/main/java/com/comparator/controller/ComparisonController.java → backend/src/main/java/com/comparator/config/AppProperties.java
- `ComparisonUploadControllerTest` --references--> `AppProperties`  [EXTRACTED]
  backend/src/test/java/com/comparator/controller/ComparisonUploadControllerTest.java → backend/src/main/java/com/comparator/config/AppProperties.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Fullstack Data Comparator Architecture** — user_requirement_data_comparator, backend_src_main_resources_application_config, frontend_readme_datacomparatorfrontend [INFERRED]

## Communities (22 total, 7 thin omitted)

### Community 0 - "dependencies"
Cohesion: 0.06
Nodes (33): @angular/cdk, @angular/common, @angular/compiler, @angular/core, @angular/forms, @angular/material, @angular/platform-browser, @angular/router (+25 more)

### Community 1 - "data-comparator-frontend"
Cohesion: 0.05
Nodes (43): build, serve, test, builder, configurations, defaultConfiguration, options, cli (+35 more)

### Community 2 - "org.junit.jupiter.api.DisplayName"
Cohesion: 0.12
Nodes (11): ComparisonUploadControllerTest, DelimiterDetectorTest, FileParserServiceTest, SqlDataSourceServiceTest, SqlValidationTests, ExcelTestUtils, org.junit.jupiter.api.DisplayName, org.junit.jupiter.api.Test (+3 more)

### Community 3 - "devDependencies"
Cohesion: 0.08
Nodes (25): @angular/build, @angular/cli, @angular/compiler-cli, devDependencies, @angular/build, @angular/cli, @angular/compiler-cli, jsdom (+17 more)

### Community 4 - "Dataset Comparison Workflow"
Cohesion: 0.18
Nodes (11): App Storage and Comparison Properties, Backend Application Configuration, H2 Database Configuration, DataComparatorFrontend Project, App Navigation and Toolbar Template, HTML Document Root, Comparison Results Dashboard, Data Comparator System Requirements (+3 more)

### Community 5 - "ComparisonRecord"
Cohesion: 0.09
Nodes (4): ComparisonRecord, jakarta.persistence.Entity, jakarta.persistence.PrePersist, jakarta.persistence.Table

### Community 6 - "App"
Cohesion: 0.33
Nodes (4): App, appConfig, routes, Component

### Community 7 - "ComparisonController.java"
Cohesion: 0.08
Nodes (29): ComparisonController, HealthController, HealthResponse, ComparisonRequest, ComparisonSummary, DatasetColumns, ToleranceConfig, UploadResponse (+21 more)

### Community 8 - "comparison.model.ts"
Cohesion: 0.05
Nodes (28): ComparisonRequest, ComparisonStatus, ComparisonSummary, DatabaseConnectionConfig, DatasetColumns, DataSourceType, MismatchDetail, MissingDetail (+20 more)

### Community 9 - "DuckDbService"
Cohesion: 0.10
Nodes (9): DelimiterDetector, DuckDbService, Override, ParquetRowWriter, FileParserService, DuckDbServiceTest, java.sql.ResultSet, org.junit.jupiter.api.BeforeEach (+1 more)

### Community 10 - "DataComparatorApplication"
Cohesion: 0.60
Nodes (3): DataComparatorApplication, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.ConfigurationPropertiesScan

### Community 12 - "FileDropzoneComponent"
Cohesion: 0.18
Nodes (3): FileDropzoneComponent, Component, ViewChild

### Community 13 - "WebConfig.java"
Cohesion: 0.43
Nodes (5): Override, WebConfig, org.springframework.context.annotation.Configuration, org.springframework.web.servlet.config.annotation.CorsRegistry, org.springframework.web.servlet.config.annotation.WebMvcConfigurer

### Community 18 - "ComparisonSqlUploadControllerTest.java"
Cohesion: 0.07
Nodes (27): AppProperties, CleanupProperties, ComparisonProperties, StorageProperties, UploadProperties, UploadConfigRequest, ComparisonRepository, AppPropertiesTest (+19 more)

### Community 19 - "DatabaseConnectionConfig"
Cohesion: 0.16
Nodes (9): DatabaseConnectionConfig, JdbcConnectionProvider, Override, PostgresJdbcConnectionProvider, SqlDataSourceService, ExecutionTests, FunctionalInterface, java.sql.Connection (+1 more)

## Knowledge Gaps
- **85 isolated node(s):** `com.comparator:data-comparator-backend`, `StorageProperties`, `UploadProperties`, `CleanupProperties`, `ComparisonProperties` (+80 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **7 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ComparisonRecord` connect `ComparisonRecord` to `ComparisonSqlUploadControllerTest.java`, `ComparisonController.java`?**
  _High betweenness centrality (0.069) - this node is a cross-community bridge._
- **Why does `FileParserService` connect `DuckDbService` to `ComparisonSqlUploadControllerTest.java`, `org.junit.jupiter.api.DisplayName`, `ComparisonController.java`?**
  _High betweenness centrality (0.027) - this node is a cross-community bridge._
- **Why does `DatabaseConnectionConfig` connect `DatabaseConnectionConfig` to `org.junit.jupiter.api.DisplayName`, `ComparisonRecord`, `ComparisonController.java`, `DuckDbService`, `ComparisonSqlUploadControllerTest.java`?**
  _High betweenness centrality (0.021) - this node is a cross-community bridge._
- **Are the 10 inferred relationships involving `DatabaseConnectionConfig` (e.g. with `.shouldHandleConnectionErrorInUpload()` and `.shouldHandleQueryTimeoutInUpload()`) actually correct?**
  _`DatabaseConnectionConfig` has 10 INFERRED edges - model-reasoned connections that need verification._
- **What connects `com.comparator:data-comparator-backend`, `StorageProperties`, `UploadProperties` to the rest of the system?**
  _85 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `dependencies` be split into smaller, more focused modules?**
  _Cohesion score 0.06060606060606061 - nodes in this community are weakly interconnected._
- **Should `data-comparator-frontend` be split into smaller, more focused modules?**
  _Cohesion score 0.04756871035940803 - nodes in this community are weakly interconnected._