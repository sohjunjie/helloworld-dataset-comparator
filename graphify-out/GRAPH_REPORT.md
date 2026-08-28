# Graph Report - helloworld-data-comparator  (2026-08-28)

## Corpus Check
- 81 files · ~26,097 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 671 nodes · 1697 edges · 31 communities (19 shown, 12 thin omitted)
- Extraction: 87% EXTRACTED · 13% INFERRED · 0% AMBIGUOUS · INFERRED: 215 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `da132cc7`
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
- comparison.model.ts
- DuckDbService
- DataComparatorApplication
- com.comparator:data-comparator-backend
- DatasetInputComponent
- WebConfig.java
- ColumnHeader
- ComparisonController
- MismatchDetail
- rxjs
- ComparisonController.java
- FileParserService
- rules/graphify.md
- workflows/graphify.md
- scripts
- package.json
- @angular/common
- @angular/compiler
- @angular/forms
- @angular/material
- @angular/platform-browser
- chart.js
- @codemirror/state

## God Nodes (most connected - your core abstractions)
1. `ComparisonRecord` - 64 edges
2. `DuckDbService` - 33 edges
3. `AppProperties` - 28 edges
4. `ComparisonController` - 26 edges
5. `ComparisonStatus` - 24 edges
6. `DatabaseConnectionConfig` - 23 edges
7. `ComparisonRepository` - 23 edges
8. `ComparisonService` - 22 edges
9. `ProgressService` - 21 edges
10. `DatasetInputComponent` - 20 edges

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

## Communities (31 total, 12 thin omitted)

### Community 0 - "dependencies"
Cohesion: 0.12
Nodes (17): @angular/cdk, @angular/core, @angular/router, codemirror, @codemirror/lang-sql, @codemirror/view, dependencies, @angular/cdk (+9 more)

### Community 1 - "data-comparator-frontend"
Cohesion: 0.05
Nodes (43): build, serve, test, builder, configurations, defaultConfiguration, options, cli (+35 more)

### Community 2 - "org.junit.jupiter.api.DisplayName"
Cohesion: 0.07
Nodes (13): ProgressUpdate, SseEmitter, AppPropertiesTest, ComparisonExecuteIntegrationTest, ComparisonResultsIntegrationTest, ComparisonUploadControllerTest, DelimiterDetectorTest, FileParserServiceTest (+5 more)

### Community 3 - "devDependencies"
Cohesion: 0.13
Nodes (15): @angular/build, @angular/cli, @angular/compiler-cli, devDependencies, @angular/build, @angular/cli, @angular/compiler-cli, jsdom (+7 more)

### Community 4 - "Dataset Comparison Workflow"
Cohesion: 0.18
Nodes (11): App Storage and Comparison Properties, Backend Application Configuration, H2 Database Configuration, DataComparatorFrontend Project, App Navigation and Toolbar Template, HTML Document Root, Comparison Results Dashboard, Data Comparator System Requirements (+3 more)

### Community 5 - "ComparisonRecord"
Cohesion: 0.07
Nodes (10): DatasetColumns, ComparisonRecord, ComparisonServiceTest, jakarta.persistence.Entity, jakarta.persistence.PrePersist, jakarta.persistence.Table, org.junit.jupiter.api.extension.ExtendWith, org.springframework.web.bind.annotation.PostMapping (+2 more)

### Community 6 - "App"
Cohesion: 0.33
Nodes (4): App, appConfig, routes, Component

### Community 7 - "DatabaseConnectionConfig"
Cohesion: 0.08
Nodes (15): DatabaseConnectionConfig, UploadConfigRequest, JdbcConnectionProvider, Override, PostgresJdbcConnectionProvider, SqlDataSourceService, ExecutionTests, SqlDataSourceServiceTest (+7 more)

### Community 8 - "comparison.model.ts"
Cohesion: 0.06
Nodes (25): ComparisonRequest, ComparisonStatus, ComparisonSummary, DatasetColumns, MismatchDetail, MissingDetail, PagedResult, ProgressUpdate (+17 more)

### Community 9 - "DuckDbService"
Cohesion: 0.08
Nodes (11): ComparisonResult, ToleranceConfig, ComparisonEngine, MismatchDetail, MissingDetail, DuckDbService, ComparisonEngineTest, DuckDbServiceTest (+3 more)

### Community 10 - "DataComparatorApplication"
Cohesion: 0.60
Nodes (3): DataComparatorApplication, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.ConfigurationPropertiesScan

### Community 12 - "DatasetInputComponent"
Cohesion: 0.06
Nodes (12): DatabaseConnectionConfig, DataSourceType, DatasetInputComponent, DelimiterOption, Component, ViewChild, FileDropzoneComponent, Component (+4 more)

### Community 13 - "WebConfig.java"
Cohesion: 0.43
Nodes (5): Override, WebConfig, org.springframework.context.annotation.Configuration, org.springframework.web.servlet.config.annotation.CorsRegistry, org.springframework.web.servlet.config.annotation.WebMvcConfigurer

### Community 15 - "ComparisonController"
Cohesion: 0.18
Nodes (9): ComparisonController, HealthController, HealthResponse, PagedResult, org.springframework.http.ResponseEntity, org.springframework.web.bind.annotation.DeleteMapping, org.springframework.web.bind.annotation.GetMapping, org.springframework.web.bind.annotation.RequestMapping (+1 more)

### Community 16 - "MismatchDetail"
Cohesion: 0.22
Nodes (4): MismatchDetail, MissingDetail, com.fasterxml.jackson.annotation.JsonInclude, com.fasterxml.jackson.annotation.JsonProperty

### Community 18 - "ComparisonController.java"
Cohesion: 0.05
Nodes (58): AppProperties, CleanupProperties, ComparisonProperties, StorageProperties, UploadProperties, ComparisonExecuteRequest, Deserializer, Override (+50 more)

### Community 19 - "FileParserService"
Cohesion: 0.17
Nodes (4): DelimiterDetector, Override, ParquetRowWriter, FileParserService

### Community 23 - "scripts"
Cohesion: 0.33
Nodes (6): scripts, build, ng, start, test, watch

### Community 24 - "package.json"
Cohesion: 0.40
Nodes (4): name, packageManager, private, version

## Knowledge Gaps
- **78 isolated node(s):** `com.comparator:data-comparator-backend`, `PENDING`, `UPLOADING`, `UPLOADED`, `CONVERTING` (+73 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **12 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ComparisonRecord` connect `ComparisonRecord` to `DuckDbService`, `ComparisonController.java`, `ComparisonController`?**
  _High betweenness centrality (0.058) - this node is a cross-community bridge._
- **Why does `DuckDbService` connect `DuckDbService` to `org.junit.jupiter.api.DisplayName`, `ComparisonRecord`, `DatabaseConnectionConfig`, `ComparisonController`, `ComparisonController.java`, `FileParserService`?**
  _High betweenness centrality (0.032) - this node is a cross-community bridge._
- **Why does `AppProperties` connect `ComparisonController.java` to `org.junit.jupiter.api.DisplayName`, `ComparisonRecord`, `ComparisonController`?**
  _High betweenness centrality (0.027) - this node is a cross-community bridge._
- **What connects `com.comparator:data-comparator-backend`, `PENDING`, `UPLOADING` to the rest of the system?**
  _78 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `dependencies` be split into smaller, more focused modules?**
  _Cohesion score 0.11764705882352941 - nodes in this community are weakly interconnected._
- **Should `data-comparator-frontend` be split into smaller, more focused modules?**
  _Cohesion score 0.04756871035940803 - nodes in this community are weakly interconnected._
- **Should `org.junit.jupiter.api.DisplayName` be split into smaller, more focused modules?**
  _Cohesion score 0.06540447504302926 - nodes in this community are weakly interconnected._