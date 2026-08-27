# 05 — PostgreSQL SQL Query Input → Parquet

**What to build:** An alternative data-input path: instead of uploading a file, the user provides a SQL SELECT query and PostgreSQL connection details (host, port, database, username, password). The backend validates the query is SELECT-only, connects to the user's PostgreSQL, executes the query with a streaming ResultSet, writes results to Parquet via DuckDB, and returns the same comparison ID + column headers response.

**Blocked by:** 03 — CSV File Upload, Delimiter Detection & Parquet Conversion

**Status:** done

- [x] `POST /api/v1/comparisons/upload` also accepts a JSON body with `ds1Sql` / `ds2Sql` query strings and `DatabaseConnectionConfig` (host, port, database, username, password) — file and SQL modes can be mixed (e.g., DS1 from file, DS2 from SQL)
- [x] `SqlDataSourceService` validates that the SQL is a SELECT statement; rejects INSERT, UPDATE, DELETE, DROP, CREATE, ALTER, etc. with a 400 error
- [x] Connects to user-provided PostgreSQL via JDBC, executes the query with streaming ResultSet
- [x] Streams rows into DuckDB and exports to Parquet at the same storage path
- [x] Column headers extracted from the ResultSet metadata
- [x] Error handling: connection refused, authentication failed, SQL syntax error, query timeout — all return appropriate HTTP error responses with descriptive messages
- [x] `ComparisonRecord.ds1Type` / `ds2Type` set to `SQL_QUERY` and source names recorded
- [x] `@SpringBootTest` + `MockMvc` tests: valid SELECT, rejected DML, connection error scenario (mocked or embedded PG)
