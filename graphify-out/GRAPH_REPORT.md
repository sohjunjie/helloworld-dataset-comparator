# Graph Report - helloworld-data-comparator  (2026-08-28)

## Corpus Check
- 76 files · ~22,492 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 594 nodes · 1407 edges · 29 communities (21 shown, 8 thin omitted)
- Extraction: 87% EXTRACTED · 13% INFERRED · 0% AMBIGUOUS · INFERRED: 182 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `e6d7beae`
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
- DatabaseConnectionConfig
- rules/graphify.md
- workflows/graphify.md
- ComparisonExecuteRequest
- FileDropzoneComponent
- CompareComponent
- ToleranceConfigComponent
- SqlEditorComponent
- compare.component.ts
- ColumnSelectorComponent

## God Nodes (most connected - your core abstractions)
1. `ComparisonRecord` - 61 edges
2. `DuckDbService` - 24 edges
3. `DatabaseConnectionConfig` - 23 edges
4. `AppProperties` - 22 edges
5. `ComparisonStatus` - 22 edges
6. `ComparisonRepository` - 21 edges
7. `DatasetInputComponent` - 20 edges
8. `ComparisonController` - 18 edges
9. `UploadConfigRequest` - 18 edges
10. `DataSourceType` - 18 edges

## Surprising Connections (you probably didn't know these)
- `App Storage and Comparison Properties` --implements--> `Parquet Large Dataset Disk Storage`  [INFERRED]
  backend/src/main/resources/application.yml → USER_REQUIREMENT.txt
- `App Navigation and Toolbar Template` --conceptually_related_to--> `Comparison Results Dashboard`  [INFERRED]
  frontend/src/app/app.html → USER_REQUIREMENT.txt
- `DataComparatorFrontend Project` --implements--> `Data Comparator System Requirements`  [INFERRED]
  frontend/README.md → USER_REQUIREMENT.txt
- `AppPropertiesTest` --references--> `AppProperties`  [EXTRACTED]
  backend/src/test/java/com/comparator/config/AppPropertiesTest.java → backend/src/main/java/com/comparator/config/AppProperties.java
- `ComparisonExecuteIntegrationTest` --references--> `AppProperties`  [EXTRACTED]
  backend/src/test/java/com/comparator/controller/ComparisonExecuteIntegrationTest.java → backend/src/main/java/com/comparator/config/AppProperties.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Fullstack Data Comparator Architecture** — user_requirement_data_comparator, backend_src_main_resources_application_config, frontend_readme_datacomparatorfrontend [INFERRED]

## Communities (29 total, 8 thin omitted)

### Community 0 - "dependencies"
Cohesion: 0.06
Nodes (33): @angular/cdk, @angular/common, @angular/compiler, @angular/core, @angular/forms, @angular/material, @angular/platform-browser, @angular/router (+25 more)

### Community 1 - "data-comparator-frontend"
Cohesion: 0.05
Nodes (43): build, serve, test, builder, configurations, defaultConfiguration, options, cli (+35 more)

### Community 2 - "org.junit.jupiter.api.DisplayName"
Cohesion: 0.07
Nodes (16): DelimiterDetector, AppPropertiesTest, ComparisonExecuteIntegrationTest, ComparisonUploadControllerTest, DelimiterDetectorTest, FileParserServiceTest, SqlDataSourceServiceTest, SqlValidationTests (+8 more)

### Community 3 - "devDependencies"
Cohesion: 0.08
Nodes (25): @angular/build, @angular/cli, @angular/compiler-cli, devDependencies, @angular/build, @angular/cli, @angular/compiler-cli, jsdom (+17 more)

### Community 4 - "Dataset Comparison Workflow"
Cohesion: 0.18
Nodes (11): App Storage and Comparison Properties, Backend Application Configuration, H2 Database Configuration, DataComparatorFrontend Project, App Navigation and Toolbar Template, HTML Document Root, Comparison Results Dashboard, Data Comparator System Requirements (+3 more)

### Community 5 - "ComparisonRecord"
Cohesion: 0.10
Nodes (4): ComparisonRecord, jakarta.persistence.Entity, jakarta.persistence.PrePersist, jakarta.persistence.Table

### Community 6 - "App"
Cohesion: 0.33
Nodes (4): App, appConfig, routes, Component

### Community 7 - "ComparisonController.java"
Cohesion: 0.06
Nodes (33): AppProperties, CleanupProperties, ComparisonProperties, StorageProperties, UploadProperties, ComparisonController, HealthController, HealthResponse (+25 more)

### Community 8 - "comparison.model.ts"
Cohesion: 0.18
Nodes (12): ComparisonRequest, ComparisonStatus, ComparisonSummary, DatasetColumns, MismatchDetail, MissingDetail, PagedResult, UploadConfigRequest (+4 more)

### Community 9 - "DuckDbService"
Cohesion: 0.08
Nodes (9): ComparisonResult, ToleranceConfig, ComparisonEngine, DuckDbService, Override, ParquetRowWriter, FileParserService, ComparisonEngineTest (+1 more)

### Community 10 - "DataComparatorApplication"
Cohesion: 0.60
Nodes (3): DataComparatorApplication, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.ConfigurationPropertiesScan

### Community 12 - "DatasetInputComponent"
Cohesion: 0.16
Nodes (6): DatabaseConnectionConfig, DataSourceType, DatasetInputComponent, DelimiterOption, Component, ViewChild

### Community 13 - "WebConfig.java"
Cohesion: 0.43
Nodes (5): Override, WebConfig, org.springframework.context.annotation.Configuration, org.springframework.web.servlet.config.annotation.CorsRegistry, org.springframework.web.servlet.config.annotation.WebMvcConfigurer

### Community 18 - "ComparisonSqlUploadControllerTest.java"
Cohesion: 0.11
Nodes (27): ComparisonRequest, ComparisonSummary, ComparisonStatus, COMPARING, COMPLETED, CONVERTING, FAILED, PENDING (+19 more)

### Community 19 - "DatabaseConnectionConfig"
Cohesion: 0.11
Nodes (10): DatabaseConnectionConfig, UploadConfigRequest, JdbcConnectionProvider, Override, PostgresJdbcConnectionProvider, ExecutionTests, com.fasterxml.jackson.annotation.JsonInclude, FunctionalInterface (+2 more)

### Community 22 - "ComparisonExecuteRequest"
Cohesion: 0.33
Nodes (7): ComparisonExecuteRequest, Deserializer, Override, com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.annotation.JsonDeserialize, com.fasterxml.jackson.databind.DeserializationContext, com.fasterxml.jackson.databind.JsonDeserializer

### Community 23 - "FileDropzoneComponent"
Cohesion: 0.18
Nodes (3): FileDropzoneComponent, Component, ViewChild

### Community 24 - "CompareComponent"
Cohesion: 0.18
Nodes (4): CompareComponent, logOrHandleError(), Component, ViewChild

### Community 25 - "ToleranceConfigComponent"
Cohesion: 0.26
Nodes (4): ToleranceConfig, ToleranceConfigComponent, ToleranceItem, Component

### Community 26 - "SqlEditorComponent"
Cohesion: 0.20
Nodes (3): SqlEditorComponent, Component, ViewChild

### Community 27 - "compare.component.ts"
Cohesion: 0.50
Nodes (3): ProgressUpdate, ProgressService, Injectable

## Knowledge Gaps
- **80 isolated node(s):** `com.comparator:data-comparator-backend`, `MismatchDetail`, `MissingDetail`, `PENDING`, `UPLOADING` (+75 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **8 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ComparisonRecord` connect `ComparisonRecord` to `ComparisonSqlUploadControllerTest.java`, `ComparisonController.java`?**
  _High betweenness centrality (0.056) - this node is a cross-community bridge._
- **Why does `FileParserService` connect `DuckDbService` to `org.junit.jupiter.api.DisplayName`, `ComparisonController.java`?**
  _High betweenness centrality (0.021) - this node is a cross-community bridge._
- **Why does `AppProperties` connect `ComparisonController.java` to `org.junit.jupiter.api.DisplayName`, `ComparisonSqlUploadControllerTest.java`?**
  _High betweenness centrality (0.018) - this node is a cross-community bridge._
- **Are the 10 inferred relationships involving `DatabaseConnectionConfig` (e.g. with `.shouldHandleConnectionErrorInUpload()` and `.shouldHandleQueryTimeoutInUpload()`) actually correct?**
  _`DatabaseConnectionConfig` has 10 INFERRED edges - model-reasoned connections that need verification._
- **What connects `com.comparator:data-comparator-backend`, `MismatchDetail`, `MissingDetail` to the rest of the system?**
  _80 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `dependencies` be split into smaller, more focused modules?**
  _Cohesion score 0.06060606060606061 - nodes in this community are weakly interconnected._
- **Should `data-comparator-frontend` be split into smaller, more focused modules?**
  _Cohesion score 0.04756871035940803 - nodes in this community are weakly interconnected._