# Graph Report - helloworld-data-comparator  (2026-08-28)

## Corpus Check
- 80 files · ~23,642 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 631 nodes · 1537 edges · 33 communities (18 shown, 15 thin omitted)
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 190 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `36dbae0e`
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
- DuckDbService
- comparison.model.ts
- .compare
- DataComparatorApplication
- com.comparator:data-comparator-backend
- DatasetInputComponent
- WebConfig.java
- ColumnHeader
- PagedResult
- MismatchDetail.java
- MissingDetail.java
- AppProperties
- DelimiterDetectorTest
- rules/graphify.md
- workflows/graphify.md
- ComparisonExecuteRequest
- scripts
- package.json
- @angular/common
- @angular/compiler
- @angular/forms
- @angular/material
- @angular/platform-browser
- chart.js
- @codemirror/state
- tslib

## God Nodes (most connected - your core abstractions)
1. `ComparisonRecord` - 62 edges
2. `AppProperties` - 28 edges
3. `DuckDbService` - 24 edges
4. `DatabaseConnectionConfig` - 23 edges
5. `ComparisonStatus` - 23 edges
6. `ComparisonRepository` - 23 edges
7. `ProgressService` - 21 edges
8. `ComparisonController` - 20 edges
9. `DatasetInputComponent` - 20 edges
10. `UploadConfigRequest` - 18 edges

## Surprising Connections (you probably didn't know these)
- `App Storage and Comparison Properties` --implements--> `Parquet Large Dataset Disk Storage`  [INFERRED]
  backend/src/main/resources/application.yml → USER_REQUIREMENT.txt
- `App Navigation and Toolbar Template` --conceptually_related_to--> `Comparison Results Dashboard`  [INFERRED]
  frontend/src/app/app.html → USER_REQUIREMENT.txt
- `DataComparatorFrontend Project` --implements--> `Data Comparator System Requirements`  [INFERRED]
  frontend/README.md → USER_REQUIREMENT.txt
- `ComparisonController` --references--> `AppProperties`  [EXTRACTED]
  backend/src/main/java/com/comparator/controller/ComparisonController.java → backend/src/main/java/com/comparator/config/AppProperties.java
- `ProgressService` --references--> `AppProperties`  [EXTRACTED]
  backend/src/main/java/com/comparator/service/ProgressService.java → backend/src/main/java/com/comparator/config/AppProperties.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Fullstack Data Comparator Architecture** — user_requirement_data_comparator, backend_src_main_resources_application_config, frontend_readme_datacomparatorfrontend [INFERRED]

## Communities (33 total, 15 thin omitted)

### Community 0 - "dependencies"
Cohesion: 0.12
Nodes (17): @angular/cdk, @angular/core, @angular/router, codemirror, @codemirror/lang-sql, @codemirror/view, dependencies, @angular/cdk (+9 more)

### Community 1 - "data-comparator-frontend"
Cohesion: 0.05
Nodes (43): build, serve, test, builder, configurations, defaultConfiguration, options, cli (+35 more)

### Community 2 - "org.junit.jupiter.api.DisplayName"
Cohesion: 0.06
Nodes (12): DatabaseConnectionConfig, ProgressUpdate, UploadConfigRequest, ComparisonExecuteIntegrationTest, DuckDbServiceTest, FileParserServiceTest, ProgressServiceTest, ExecutionTests (+4 more)

### Community 3 - "devDependencies"
Cohesion: 0.13
Nodes (15): @angular/build, @angular/cli, @angular/compiler-cli, devDependencies, @angular/build, @angular/cli, @angular/compiler-cli, jsdom (+7 more)

### Community 4 - "Dataset Comparison Workflow"
Cohesion: 0.18
Nodes (11): App Storage and Comparison Properties, Backend Application Configuration, H2 Database Configuration, DataComparatorFrontend Project, App Navigation and Toolbar Template, HTML Document Root, Comparison Results Dashboard, Data Comparator System Requirements (+3 more)

### Community 5 - "ComparisonRecord"
Cohesion: 0.07
Nodes (17): ComparisonController, HealthController, HealthResponse, ComparisonSummary, DatasetColumns, UploadResponse, ComparisonRecord, jakarta.persistence.Entity (+9 more)

### Community 6 - "App"
Cohesion: 0.33
Nodes (4): App, appConfig, routes, Component

### Community 7 - "DuckDbService"
Cohesion: 0.07
Nodes (20): DuckDbService, Override, ParquetRowWriter, FileParserService, JdbcConnectionProvider, Override, PostgresJdbcConnectionProvider, SseEmitter (+12 more)

### Community 8 - "comparison.model.ts"
Cohesion: 0.06
Nodes (25): ComparisonRequest, ComparisonStatus, ComparisonSummary, DatasetColumns, MismatchDetail, MissingDetail, PagedResult, ProgressUpdate (+17 more)

### Community 9 - ".compare"
Cohesion: 0.21
Nodes (3): ToleranceConfig, ComparisonEngine, ComparisonEngineTest

### Community 10 - "DataComparatorApplication"
Cohesion: 0.60
Nodes (3): DataComparatorApplication, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.ConfigurationPropertiesScan

### Community 12 - "DatasetInputComponent"
Cohesion: 0.06
Nodes (12): DatabaseConnectionConfig, DataSourceType, DatasetInputComponent, DelimiterOption, Component, ViewChild, FileDropzoneComponent, Component (+4 more)

### Community 13 - "WebConfig.java"
Cohesion: 0.43
Nodes (5): Override, WebConfig, org.springframework.context.annotation.Configuration, org.springframework.web.servlet.config.annotation.CorsRegistry, org.springframework.web.servlet.config.annotation.WebMvcConfigurer

### Community 18 - "AppProperties"
Cohesion: 0.06
Nodes (49): AppProperties, CleanupProperties, ComparisonProperties, StorageProperties, UploadProperties, ComparisonRequest, ComparisonResult, ComparisonStatus (+41 more)

### Community 19 - "DelimiterDetectorTest"
Cohesion: 0.14
Nodes (7): DelimiterDetector, DelimiterDetectorTest, SqlValidationTests, org.junit.jupiter.api.Nested, org.junit.jupiter.params.ParameterizedTest, org.junit.jupiter.params.provider.CsvSource, org.junit.jupiter.params.provider.ValueSource

### Community 22 - "ComparisonExecuteRequest"
Cohesion: 0.33
Nodes (7): ComparisonExecuteRequest, Deserializer, Override, com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.annotation.JsonDeserialize, com.fasterxml.jackson.databind.DeserializationContext, com.fasterxml.jackson.databind.JsonDeserializer

### Community 23 - "scripts"
Cohesion: 0.33
Nodes (6): scripts, build, ng, start, test, watch

### Community 24 - "package.json"
Cohesion: 0.40
Nodes (4): name, packageManager, private, version

## Knowledge Gaps
- **80 isolated node(s):** `com.comparator:data-comparator-backend`, `MismatchDetail`, `MissingDetail`, `PENDING`, `UPLOADING` (+75 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **15 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ComparisonRecord` connect `ComparisonRecord` to `AppProperties`, `org.junit.jupiter.api.DisplayName`?**
  _High betweenness centrality (0.055) - this node is a cross-community bridge._
- **Why does `AppProperties` connect `AppProperties` to `org.junit.jupiter.api.DisplayName`, `ComparisonRecord`, `DuckDbService`?**
  _High betweenness centrality (0.025) - this node is a cross-community bridge._
- **Why does `FileParserService` connect `DuckDbService` to `org.junit.jupiter.api.DisplayName`, `AppProperties`, `DelimiterDetectorTest`, `ComparisonRecord`?**
  _High betweenness centrality (0.020) - this node is a cross-community bridge._
- **What connects `com.comparator:data-comparator-backend`, `MismatchDetail`, `MissingDetail` to the rest of the system?**
  _80 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `dependencies` be split into smaller, more focused modules?**
  _Cohesion score 0.11764705882352941 - nodes in this community are weakly interconnected._
- **Should `data-comparator-frontend` be split into smaller, more focused modules?**
  _Cohesion score 0.04756871035940803 - nodes in this community are weakly interconnected._
- **Should `org.junit.jupiter.api.DisplayName` be split into smaller, more focused modules?**
  _Cohesion score 0.05757286192068801 - nodes in this community are weakly interconnected._