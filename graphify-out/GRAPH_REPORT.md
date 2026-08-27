# Graph Report - helloworld-data-comparator  (2026-08-28)

## Corpus Check
- 34 files · ~3,848 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 264 nodes · 403 edges · 18 communities (13 shown, 5 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 41 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `8d35acc4`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- dependencies
- options
- data-comparator-frontend
- devDependencies
- Dataset Comparison Workflow
- ComparisonRecord
- App
- ComparisonController.java
- ComparisonControllerTest.java
- ComparisonStatus
- DataComparatorApplication
- com.comparator:data-comparator-backend
- AppProperties
- WebConfig.java
- ColumnHeader
- PagedResult
- MismatchDetail.java
- MissingDetail.java

## God Nodes (most connected - your core abstractions)
1. `ComparisonRecord` - 52 edges
2. `ComparisonStatus` - 16 edges
3. `DataSourceType` - 15 edges
4. `ComparisonRepository` - 12 edges
5. `ComparisonControllerTest` - 10 edges
6. `ComparisonRequest` - 9 edges
7. `ComparisonSummary` - 9 edges
8. `ComparisonController` - 8 edges
9. `AppProperties` - 7 edges
10. `data-comparator-frontend` - 7 edges

## Surprising Connections (you probably didn't know these)
- `App Storage and Comparison Properties` --implements--> `Parquet Large Dataset Disk Storage`  [INFERRED]
  backend/src/main/resources/application.yml → USER_REQUIREMENT.txt
- `App Navigation and Toolbar Template` --conceptually_related_to--> `Comparison Results Dashboard`  [INFERRED]
  frontend/src/app/app.html → USER_REQUIREMENT.txt
- `DataComparatorFrontend Project` --implements--> `Data Comparator System Requirements`  [INFERRED]
  frontend/README.md → USER_REQUIREMENT.txt
- `AppPropertiesTest` --references--> `AppProperties`  [EXTRACTED]
  backend/src/test/java/com/comparator/config/AppPropertiesTest.java → backend/src/main/java/com/comparator/config/AppProperties.java
- `ComparisonRequest` --references--> `DatabaseConnectionConfig`  [EXTRACTED]
  backend/src/main/java/com/comparator/model/dto/ComparisonRequest.java → backend/src/main/java/com/comparator/model/dto/DatabaseConnectionConfig.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Fullstack Data Comparator Architecture** — user_requirement_data_comparator, backend_src_main_resources_application_config, frontend_readme_datacomparatorfrontend [INFERRED]

## Communities (18 total, 5 thin omitted)

### Community 0 - "dependencies"
Cohesion: 0.06
Nodes (33): @angular/cdk, @angular/common, @angular/compiler, @angular/core, @angular/forms, @angular/material, @angular/platform-browser, @angular/router (+25 more)

### Community 1 - "options"
Cohesion: 0.07
Nodes (29): build, serve, test, builder, configurations, defaultConfiguration, options, development (+21 more)

### Community 2 - "data-comparator-frontend"
Cohesion: 0.13
Nodes (14): cli, packageManager, prefix, projectType, root, schematics, sourceRoot, newProjectRoot (+6 more)

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
Nodes (4): Component, App, appConfig, routes

### Community 7 - "ComparisonController.java"
Cohesion: 0.13
Nodes (12): ComparisonController, HealthController, HealthResponse, ComparisonSummary, ComparisonRepository, org.springframework.data.jpa.repository.JpaRepository, org.springframework.http.ResponseEntity, org.springframework.stereotype.Repository (+4 more)

### Community 8 - "ComparisonControllerTest.java"
Cohesion: 0.19
Nodes (12): ComparisonRequest, DatabaseConnectionConfig, ToleranceConfig, AppPropertiesTest, ComparisonControllerTest, DataComparatorApplicationTests, com.fasterxml.jackson.databind.ObjectMapper, org.junit.jupiter.api.DisplayName (+4 more)

### Community 9 - "ComparisonStatus"
Cohesion: 0.13
Nodes (13): ComparisonStatus, COMPARING, COMPLETED, CONVERTING, FAILED, PENDING, UPLOADING, DataSourceType (+5 more)

### Community 10 - "DataComparatorApplication"
Cohesion: 0.60
Nodes (3): DataComparatorApplication, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.ConfigurationPropertiesScan

### Community 12 - "AppProperties"
Cohesion: 0.33
Nodes (6): AppProperties, CleanupProperties, ComparisonProperties, StorageProperties, UploadProperties, org.springframework.boot.context.properties.ConfigurationProperties

### Community 13 - "WebConfig.java"
Cohesion: 0.43
Nodes (5): WebConfig, org.springframework.context.annotation.Configuration, org.springframework.web.servlet.config.annotation.CorsRegistry, org.springframework.web.servlet.config.annotation.WebMvcConfigurer, Override

## Knowledge Gaps
- **78 isolated node(s):** `com.comparator:data-comparator-backend`, `StorageProperties`, `UploadProperties`, `CleanupProperties`, `ComparisonProperties` (+73 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **5 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ComparisonRecord` connect `ComparisonRecord` to `ComparisonControllerTest.java`, `ComparisonStatus`, `ComparisonController.java`?**
  _High betweenness centrality (0.098) - this node is a cross-community bridge._
- **Why does `dependencies` connect `dependencies` to `devDependencies`?**
  _High betweenness centrality (0.038) - this node is a cross-community bridge._
- **Why does `AppPropertiesTest` connect `ComparisonControllerTest.java` to `AppProperties`?**
  _High betweenness centrality (0.022) - this node is a cross-community bridge._
- **What connects `com.comparator:data-comparator-backend`, `StorageProperties`, `UploadProperties` to the rest of the system?**
  _78 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `dependencies` be split into smaller, more focused modules?**
  _Cohesion score 0.06060606060606061 - nodes in this community are weakly interconnected._
- **Should `options` be split into smaller, more focused modules?**
  _Cohesion score 0.07389162561576355 - nodes in this community are weakly interconnected._
- **Should `data-comparator-frontend` be split into smaller, more focused modules?**
  _Cohesion score 0.13333333333333333 - nodes in this community are weakly interconnected._