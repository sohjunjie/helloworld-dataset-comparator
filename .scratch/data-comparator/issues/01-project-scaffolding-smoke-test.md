# 01 — Project Scaffolding + Smoke Test

**What to build:** Both projects scaffolded inside the monorepo — a Spring Boot 3.4 backend (`backend/`) and an Angular 22 standalone-component frontend (`frontend/`). The backend exposes a trivial health endpoint (`GET /api/health → 200`). The frontend is generated with routing enabled, SCSS styles, and SSR disabled. A `proxy.conf.json` forwards `/api/**` to `localhost:8080`. Starting both apps and hitting the health endpoint from the Angular dev server proves the round-trip works.

**Blocked by:** None — can start immediately.

**Status:** done

- [x] `backend/pom.xml` exists with Spring Boot 3.4 parent, Java 21 target, and all dependencies (spring-boot-starter-web, spring-boot-starter-data-jpa, H2, DuckDB JDBC, PostgreSQL JDBC, Apache POI, spring-boot-starter-validation, spring-boot-starter-test)
- [x] `backend/src/main/resources/application.yml` configures H2 datasource, multipart limits (500 MB), and `app.*` custom properties (storage path, TTL, timeout)
- [x] `DataComparatorApplication.java` with `@SpringBootApplication` compiles and boots cleanly
- [x] A `GET /api/health` controller returns HTTP 200 with a JSON body
- [x] `frontend/` generated via Angular CLI 22 with `--routing --ssr=false --style=scss`
- [x] Angular Material added, Chart.js + ng2-charts installed, CodeMirror 6 packages installed
- [x] `frontend/proxy.conf.json` forwards `/api/**` to `http://localhost:8080`
- [x] `backend/` compiles: `mvn clean compile` exits 0
- [x] `frontend/` builds: `npm run build` exits 0
- [x] With both running, `http://localhost:4200/api/health` returns the backend's 200 response
