# Graph Report - helloworld-data-comparator  (2026-08-28)

## Corpus Check
- 60 files · ~13,538 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 455 nodes · 887 edges · 19 communities (13 shown, 6 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 91 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `874c880b`
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
- .upload
- comparison.model.ts
- FileParserService
- DataComparatorApplication
- com.comparator:data-comparator-backend
- FileDropzoneComponent
- WebConfig.java
- ColumnHeader
- PagedResult
- MismatchDetail.java
- MissingDetail.java
- ColumnSelectorComponent

## God Nodes (most connected - your core abstractions)
1. `ComparisonRecord` - 53 edges
2. `ComparisonStatus` - 18 edges
3. `DataSourceType` - 17 edges
4. `FileParserService` - 17 edges
5. `FileParserServiceTest` - 16 edges
6. `DuckDbService` - 15 edges
7. `ComparisonUploadControllerTest` - 15 edges
8. `DelimiterDetectorTest` - 15 edges
9. `ComparisonService` - 15 edges
10. `ComparisonRepository` - 14 edges

## Surprising Connections (you probably didn't know these)
- `App Storage and Comparison Properties` --implements--> `Parquet Large Dataset Disk Storage`  [INFERRED]
  backend/src/main/resources/application.yml → USER_REQUIREMENT.txt
- `App Navigation and Toolbar Template` --conceptually_related_to--> `Comparison Results Dashboard`  [INFERRED]
  frontend/src/app/app.html → USER_REQUIREMENT.txt
- `DataComparatorFrontend Project` --implements--> `Data Comparator System Requirements`  [INFERRED]
  frontend/README.md → USER_REQUIREMENT.txt
- `AppPropertiesTest` --references--> `AppProperties`  [EXTRACTED]
  backend/src/test/java/com/comparator/config/AppPropertiesTest.java → backend/src/main/java/com/comparator/config/AppProperties.java
- `ComparisonUploadControllerTest` --references--> `AppProperties`  [EXTRACTED]
  backend/src/test/java/com/comparator/controller/ComparisonUploadControllerTest.java → backend/src/main/java/com/comparator/config/AppProperties.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Fullstack Data Comparator Architecture** — user_requirement_data_comparator, backend_src_main_resources_application_config, frontend_readme_datacomparatorfrontend [INFERRED]

## Communities (19 total, 6 thin omitted)

### Community 0 - "dependencies"
Cohesion: 0.06
Nodes (33): @angular/cdk, @angular/common, @angular/compiler, @angular/core, @angular/forms, @angular/material, @angular/platform-browser, @angular/router (+25 more)

### Community 1 - "data-comparator-frontend"
Cohesion: 0.05
Nodes (43): build, serve, test, builder, configurations, defaultConfiguration, options, cli (+35 more)

### Community 2 - "org.junit.jupiter.api.Test"
Cohesion: 0.09
Nodes (17): AppPropertiesTest, ComparisonControllerTest, ComparisonUploadControllerTest, MaxFileSizeLimitTest, DataComparatorApplicationTests, DelimiterDetectorTest, FileParserServiceTest, ExcelTestUtils (+9 more)

### Community 3 - "devDependencies"
Cohesion: 0.08
Nodes (25): @angular/build, @angular/cli, @angular/compiler-cli, devDependencies, @angular/build, @angular/cli, @angular/compiler-cli, jsdom (+17 more)

### Community 4 - "Dataset Comparison Workflow"
Cohesion: 0.18
Nodes (11): App Storage and Comparison Properties, Backend Application Configuration, H2 Database Configuration, DataComparatorFrontend Project, App Navigation and Toolbar Template, HTML Document Root, Comparison Results Dashboard, Data Comparator System Requirements (+3 more)

### Community 5 - "ComparisonRecord"
Cohesion: 0.06
Nodes (20): ComparisonRequest, ComparisonSummary, DatabaseConnectionConfig, ToleranceConfig, ComparisonRecord, ComparisonStatus, COMPARING, COMPLETED (+12 more)

### Community 6 - "App"
Cohesion: 0.33
Nodes (4): App, appConfig, routes, Component

### Community 7 - ".upload"
Cohesion: 0.06
Nodes (25): AppProperties, CleanupProperties, ComparisonProperties, StorageProperties, UploadProperties, ComparisonController, HealthController, HealthResponse (+17 more)

### Community 8 - "comparison.model.ts"
Cohesion: 0.07
Nodes (22): ComparisonRequest, ComparisonStatus, ComparisonSummary, DatabaseConnectionConfig, DatasetColumns, MismatchDetail, MissingDetail, PagedResult (+14 more)

### Community 9 - "FileParserService"
Cohesion: 0.11
Nodes (10): DelimiterDetector, DuckDbService, Override, ParquetRowWriter, FileParserService, DuckDbServiceTest, java.sql.Connection, org.junit.jupiter.api.BeforeEach (+2 more)

### Community 10 - "DataComparatorApplication"
Cohesion: 0.60
Nodes (3): DataComparatorApplication, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.ConfigurationPropertiesScan

### Community 12 - "FileDropzoneComponent"
Cohesion: 0.11
Nodes (7): DataSourceType, DatasetInputComponent, DelimiterOption, Component, FileDropzoneComponent, Component, ViewChild

### Community 13 - "WebConfig.java"
Cohesion: 0.43
Nodes (5): Override, WebConfig, org.springframework.context.annotation.Configuration, org.springframework.web.servlet.config.annotation.CorsRegistry, org.springframework.web.servlet.config.annotation.WebMvcConfigurer

## Knowledge Gaps
- **83 isolated node(s):** `com.comparator:data-comparator-backend`, `StorageProperties`, `UploadProperties`, `CleanupProperties`, `ComparisonProperties` (+78 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **6 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ComparisonRecord` connect `ComparisonRecord` to `.upload`?**
  _High betweenness centrality (0.066) - this node is a cross-community bridge._
- **Why does `FileParserService` connect `FileParserService` to `org.junit.jupiter.api.Test`, `ComparisonRecord`, `.upload`?**
  _High betweenness centrality (0.038) - this node is a cross-community bridge._
- **Why does `ComparisonStatus` connect `ComparisonRecord` to `.upload`?**
  _High betweenness centrality (0.017) - this node is a cross-community bridge._
- **What connects `com.comparator:data-comparator-backend`, `StorageProperties`, `UploadProperties` to the rest of the system?**
  _83 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `dependencies` be split into smaller, more focused modules?**
  _Cohesion score 0.06060606060606061 - nodes in this community are weakly interconnected._
- **Should `data-comparator-frontend` be split into smaller, more focused modules?**
  _Cohesion score 0.04756871035940803 - nodes in this community are weakly interconnected._
- **Should `org.junit.jupiter.api.Test` be split into smaller, more focused modules?**
  _Cohesion score 0.09077380952380952 - nodes in this community are weakly interconnected._