# Agents Learnings

This document records agent self-reflections, post-mortem root causes from past executions, and generic software engineering / operational best practices.

---

## 1. Post-Mortem & Agent Operational Principles

- **Windows CLI Tooling and Process Lifecycle**: Long-running background server processes (e.g. dev servers and Spring Boot instances) started via background tasks must be explicitly terminated after health/smoke verification to prevent background port locking and resource consumption.
- **Issue Tracking & Spec Synchronization**: When implementing work from issue tickets or specification files (e.g. `.scratch/**/issues/*.md`), updating the originating issue artifact (checking off completed requirement items `[x]` and updating `Status: done`) is a mandatory, atomic part of the pre-commit and task completion lifecycle.

---

## 2. Architecture, Styling & UI Best Practices

- **Dev Proxy Alignment**: Always configure dev server proxying (e.g., `proxy.conf.json`) in both `angular.json` options and startup scripts so full round-trip API calls succeed seamlessly in both CLI and browser contexts without CORS overhead.
- **Angular Material Theme & Overlay Backgrounds**: Always include `@include mat.all-component-themes($theme)` or theme mixins in global `styles.scss` and explicitly define solid background styling for `.mat-mdc-select-panel` overlays to prevent dropdown menus from rendering transparently.


---

## 3. TypeScript, Testing & Algorithm Best Practices

- **Standalone Component Test Isolation**: In Angular standalone component testing, provide lightweight stub routing via `provideRouter([])` in test bed configuration to isolate navigation dependencies while testing presentation elements.
- **JSDOM Drag-and-Drop Test Isolation**: In Node/JSDOM component unit tests where browser `DragEvent` and `DataTransfer` globals are not natively instantiated, test dropzone handler methods directly with synthetic event object mocks to prevent DOM API reference errors.
- **Embedded Database Test Isolation**: In Spring Boot JPA test environments, configure in-memory H2 datasources with `create-drop` in test profiles/resources to ensure clean schema generation and prevent schema/enum check-constraint drift against local file-based database artifacts.
- **JDK Interface Test Isolation via Lightweight Dynamic Proxies or In-Memory Stubs**: When testing standard JDK library interfaces (such as `java.sql.Connection` or `java.sql.Statement`) across varying JVM versions, favor using embedded in-memory databases (e.g. H2) or standard JDK reflection `Proxy.newProxyInstance` instead of inline bytecode-instrumented mocks to avoid bytecode-manipulation constraints in test runners.
