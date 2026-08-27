# Graph Report - helloworld-data-comparator  (2026-08-28)

## Corpus Check
- 44 files · ~6,393 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 341 nodes · 657 edges · 17 communities (12 shown, 5 thin omitted)
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 79 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `017c8878`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- dependencies
- data-comparator-frontend
- org.junit.jupiter.api.Test
- devDependencies
- Dataset Comparison Workflow
- ComparisonRecord
- App
- ComparisonController.java
- ComparisonUploadControllerTest.java
- DuckDbService
- DataComparatorApplication
- com.comparator:data-comparator-backend
- WebConfig.java
- ColumnHeader
- PagedResult
- MismatchDetail.java
- MissingDetail.java

## God Nodes (most connected - your core abstractions)
1. `ComparisonRecord` - 53 edges
2. `ComparisonStatus` - 18 edges
3. `DataSourceType` - 17 edges
4. `DelimiterDetectorTest` - 15 edges
5. `ComparisonRepository` - 14 edges
6. `ComparisonController` - 13 edges
7. `DuckDbService` - 13 edges
8. `ComparisonUploadControllerTest` - 13 edges
9. `AppProperties` - 12 edges
10. `FileParserService` - 12 edges

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

## Communities (17 total, 5 thin omitted)

### Community 0 - "dependencies"
Cohesion: 0.06
Nodes (33): @angular/cdk, @angular/common, @angular/compiler, @angular/core, @angular/forms, @angular/material, @angular/platform-browser, @angular/router (+25 more)

### Community 1 - "data-comparator-frontend"
Cohesion: 0.05
Nodes (43): build, serve, test, builder, configurations, defaultConfiguration, options, cli (+35 more)

### Community 2 - "org.junit.jupiter.api.Test"
Cohesion: 0.15
Nodes (7): AppPropertiesTest, DelimiterDetectorTest, FileParserServiceTest, org.junit.jupiter.api.DisplayName, org.junit.jupiter.api.Test, org.junit.jupiter.params.ParameterizedTest, org.junit.jupiter.params.provider.CsvSource

### Community 3 - "devDependencies"
Cohesion: 0.08
Nodes (25): @angular/build, @angular/cli, @angular/compiler-cli, devDependencies, @angular/build, @angular/cli, @angular/compiler-cli, jsdom (+17 more)

### Community 4 - "Dataset Comparison Workflow"
Cohesion: 0.18
Nodes (11): App Storage and Comparison Properties, Backend Application Configuration, H2 Database Configuration, DataComparatorFrontend Project, App Navigation and Toolbar Template, HTML Document Root, Comparison Results Dashboard, Data Comparator System Requirements (+3 more)

### Community 5 - "ComparisonRecord"
Cohesion: 0.06
Nodes (19): ComparisonRequest, ComparisonSummary, DatabaseConnectionConfig, ToleranceConfig, ComparisonRecord, ComparisonStatus, COMPARING, COMPLETED (+11 more)

### Community 6 - "App"
Cohesion: 0.33
Nodes (4): App, appConfig, routes, Component

### Community 7 - "ComparisonController.java"
Cohesion: 0.09
Nodes (18): ComparisonController, HealthController, HealthResponse, DatasetColumns, UploadConfigRequest, UploadResponse, ComparisonRepository, ComparisonRepositoryTest (+10 more)

### Community 8 - "ComparisonUploadControllerTest.java"
Cohesion: 0.17
Nodes (16): AppProperties, CleanupProperties, ComparisonProperties, StorageProperties, UploadProperties, ComparisonControllerTest, ComparisonUploadControllerTest, MaxFileSizeLimitTest (+8 more)

### Community 9 - "DuckDbService"
Cohesion: 0.15
Nodes (8): DelimiterDetector, DuckDbService, FileParserService, DuckDbServiceTest, java.sql.Connection, org.junit.jupiter.api.BeforeEach, org.springframework.stereotype.Component, org.springframework.stereotype.Service

### Community 10 - "DataComparatorApplication"
Cohesion: 0.60
Nodes (3): DataComparatorApplication, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.ConfigurationPropertiesScan

### Community 13 - "WebConfig.java"
Cohesion: 0.43
Nodes (5): WebConfig, org.springframework.context.annotation.Configuration, org.springframework.web.servlet.config.annotation.CorsRegistry, org.springframework.web.servlet.config.annotation.WebMvcConfigurer, Override

## Knowledge Gaps
- **79 isolated node(s):** `com.comparator:data-comparator-backend`, `StorageProperties`, `UploadProperties`, `CleanupProperties`, `ComparisonProperties` (+74 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **5 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ComparisonRecord` connect `ComparisonRecord` to `ComparisonUploadControllerTest.java`, `ComparisonController.java`?**
  _High betweenness centrality (0.101) - this node is a cross-community bridge._
- **Why does `FileParserService` connect `DuckDbService` to `ComparisonUploadControllerTest.java`, `org.junit.jupiter.api.Test`, `ComparisonController.java`?**
  _High betweenness centrality (0.037) - this node is a cross-community bridge._
- **Why does `ComparisonStatus` connect `ComparisonRecord` to `ComparisonUploadControllerTest.java`, `ComparisonController.java`?**
  _High betweenness centrality (0.026) - this node is a cross-community bridge._
- **What connects `com.comparator:data-comparator-backend`, `StorageProperties`, `UploadProperties` to the rest of the system?**
  _79 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `dependencies` be split into smaller, more focused modules?**
  _Cohesion score 0.06060606060606061 - nodes in this community are weakly interconnected._
- **Should `data-comparator-frontend` be split into smaller, more focused modules?**
  _Cohesion score 0.04756871035940803 - nodes in this community are weakly interconnected._
- **Should `devDependencies` be split into smaller, more focused modules?**
  _Cohesion score 0.07692307692307693 - nodes in this community are weakly interconnected._