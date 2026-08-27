# Agents Learnings

This document records agent self-reflections, post-mortem root causes from past executions, and generic software engineering / operational best practices.

---

## 1. Post-Mortem & Agent Operational Principles

- **Windows CLI Tooling and Process Lifecycle**: Long-running background server processes (e.g. dev servers and Spring Boot instances) started via background tasks must be explicitly terminated after health/smoke verification to prevent background port locking and resource consumption.

---

## 2. Architecture, Styling & UI Best Practices

- **Dev Proxy Alignment**: Always configure dev server proxying (e.g., `proxy.conf.json`) in both `angular.json` options and startup scripts so full round-trip API calls succeed seamlessly in both CLI and browser contexts without CORS overhead.

---

## 3. TypeScript, Testing & Algorithm Best Practices

- **Standalone Component Test Isolation**: In Angular standalone component testing, provide lightweight stub routing via `provideRouter([])` in test bed configuration to isolate navigation dependencies while testing presentation elements.
