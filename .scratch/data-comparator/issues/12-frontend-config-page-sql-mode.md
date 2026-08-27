# 12 — Frontend: Configuration Page (SQL Query Mode)

**What to build:** Enable the SQL query input mode on the dataset input panels. When the user toggles to "SQL Query", a CodeMirror 6 editor with SQL syntax highlighting appears alongside PostgreSQL connection detail fields. Submitting SQL input flows through the same upload → column detection → compare pipeline as file upload.

**Blocked by:** 05 — PostgreSQL SQL Query Input → Parquet; 11 — Frontend: Configuration Page (File Upload Mode)

**Status:** ready-for-agent

- [ ] `sql-editor` standalone component wraps CodeMirror 6 with SQL language support (`@codemirror/lang-sql`)
- [ ] Editor has a reasonable minimum height, line numbers, and SQL syntax highlighting
- [ ] When "SQL Query" radio is selected, the file dropzone hides and the SQL editor + connection fields appear
- [ ] PostgreSQL connection fields in a `mat-expansion-panel`: Host, Port (default 5432), Database, Username, Password (type=password) — all `mat-form-field` with required validation
- [ ] The two dataset panels can independently be in file or SQL mode (e.g., DS1 from file, DS2 from SQL)
- [ ] "Upload" button sends the SQL query + connection config as JSON alongside any file uploads in the multipart request
- [ ] Column headers returned from the backend populate the same `column-selector` component
- [ ] Angular `TestBed` component test: toggles to SQL mode, fills in editor and connection fields, mocks HTTP response, verifies column selector population
