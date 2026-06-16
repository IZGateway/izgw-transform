## 1. Project Setup and Dependencies

- [ ] 1.1 Add `spring-boot-starter-data-jdbc` and `spring-boot-starter-jdbc` to `pom.xml`
- [ ] 1.2 Add `com.h2database:h2` (test scope) to `pom.xml`
- [ ] 1.3 Add `com.microsoft.sqlserver:mssql-jdbc` (runtime, optional) to `pom.xml`
- [ ] 1.4 Add `spring-boot-starter-validation` if not already present (for config validation)
- [ ] 1.5 Create package `gov.cdc.izgateway.xform.sql` for all new SQL back-end code
- [ ] 1.6 Create package `gov.cdc.izgateway.xform.sql.bulk` for Bulk FHIR export code

## 2. Reserved Destination and Configuration

- [ ] 2.1 Add `"sql"` to `XformConstants` as a reserved destination constant
- [ ] 2.2 Update destination validation logic to reject registration of destinations with reserved names (check `XformConstants.RESERVED_DESTINATIONS`)
- [ ] 2.3 Create `SqlBackendProperties` `@ConfigurationProperties(prefix = "sql")` class with fields: `enabled` (auto-derived from datasource presence), `matching.threshold` (double, default matches existing IDI threshold), `tables.patient` (String), `tables.immunization` (String), `mapping.configPath` (String)
- [ ] 2.4 Create `SqlBackendAutoConfiguration` `@ConditionalOnProperty`/`@ConditionalOnBean(DataSource.class)` that instantiates `SqlFhirBackend` only when a datasource is configured
- [ ] 2.5 Add `application.yml` documentation comments for the new `sql.*` config block

## 3. Query Backend Interface Abstraction

- [ ] 3.1 Create `IQueryBackend` interface with `boolean supports(String destinationId)` and `Bundle query(Patient searchPatient, String destinationId, HttpServletRequest req)` methods
- [ ] 3.2 Create `IzGatewayQueryBackend implements IQueryBackend` wrapping the existing `HubController` / IIS path — `supports()` returns `true` for any non-reserved destination
- [ ] 3.3 Inject `List<IQueryBackend>` into `FhirController`; replace direct `hub.submitSingleMessage()` call with `backends.stream().filter(b -> b.supports(dest)).findFirst().orElseThrow()`
- [ ] 3.4 Verify existing integration tests still pass after `IzGatewayQueryBackend` wrapper is applied

## 4. SQL Patient Row Mapping

- [ ] 4.1 Create `SqlPatientRowMapper` that converts a `Map<String, Object>` SQL row to a HAPI FHIR `Patient` object using column names from `SqlBackendProperties.tables` configuration
- [ ] 4.2 Map patient demographics: first name, middle name, last name, DOB, gender, address (street1, street2, city, state, ZIP), phone, email, MRN/patient ID as an `Identifier`
- [ ] 4.3 Write unit tests for `SqlPatientRowMapper` covering each mapped field and null/missing column handling

## 5. SQL Patient Search and IDIMatch Integration

- [ ] 5.1 Create `SqlPatientSearchService` with `JdbcTemplate`; implement a broad ANSI SQL candidate query using name + DOB OR DOB + gender to return candidate rows
- [ ] 5.2 Ensure all query parameters are bound via named JDBC parameters (no string concatenation)
- [ ] 5.3 Call `SqlPatientRowMapper` to convert each candidate row to a FHIR `Patient`
- [ ] 5.4 Call existing `IDIMatch` static scoring methods on each candidate `Patient` vs the search `Patient`
- [ ] 5.5 Implement singular-match enforcement: return matched patient ID if exactly one candidate ≥ threshold; return empty bundle if zero; return ambiguous `OperationOutcome` if two or more
- [ ] 5.6 Write unit tests for `SqlPatientSearchService` using an in-memory H2 datasource with a small synthetic patient table

## 6. SQL Immunization Retrieval

- [ ] 6.1 Create `SqlImmunizationRetrievalService` with `JdbcTemplate`; implement parameterized ANSI SQL query for all immunization rows by patient ID
- [ ] 6.2 Return results as `List<Map<String, Object>>` with column names preserved
- [ ] 6.3 Write unit tests for `SqlImmunizationRetrievalService` using H2 fixture

## 7. Tabular-to-FHIR Conversion Configuration

- [ ] 7.1 Create `SqlMappingConfig` model classes: `SqlMappingConfiguration` (top-level), `ResourceMapping` (per column), `ConceptMapEntry` (`from`/`to`)
- [ ] 7.2 Implement `SqlMappingConfigLoader` that loads `sql-mapping.yml` from classpath or external `sql.mapping.config-path`; use Jackson YAML or SnakeYAML
- [ ] 7.3 Write the default `sql-mapping.yml` in `src/main/resources/` covering all 61 fields in `all_vax_event_enriched_mapping.csv` mapped to Patient and Immunization resources
- [ ] 7.4 Create `TabularFhirConverter` that, given a `List<Map<String, Object>>` and a `SqlMappingConfiguration`, produces a FHIR `Bundle` of type `searchset`
- [ ] 7.5 Implement primitive type converters: `string`, `integer`, `decimal`, `boolean`, `date`, `dateTime`
- [ ] 7.6 Implement complex type converters: `code`, `Coding` (with system), `CodeableConcept` (with system + optional display)
- [ ] 7.7 Implement concept mapping lookup (from → to with optional default passthrough)
- [ ] 7.8 Implement FHIRPath-based element assignment using HAPI `FhirPathEngine` for non-trivial paths; use direct setters for common high-performance paths (name.family, birthDate, etc.)
- [ ] 7.9 Ensure a single `Patient` resource is created per bundle; all `Immunization` resources reference it
- [ ] 7.10 Write unit tests for `TabularFhirConverter` covering each datatype and concept mapping

## 8. SqlFhirBackend — Single-Patient Query Assembly

- [ ] 8.1 Create `SqlFhirBackend implements IQueryBackend`; `supports()` returns `true` only for `"sql"`
- [ ] 8.2 Wire together: `SqlPatientSearchService` → `SqlImmunizationRetrievalService` → `TabularFhirConverter`
- [ ] 8.3 Return resulting `Bundle` through `FhirController`'s existing response serialization path
- [ ] 8.4 Write integration test using H2 fixture + FHIR HTTP client verifying end-to-end single-patient query response

## 9. H2 Test Fixture

- [ ] 9.1 Create `src/test/resources/schema-h2.sql` defining `patients` and `immunizations` tables with column names matching the default mapping config
- [ ] 9.2 Create `src/test/resources/application-test.yml` with H2 datasource config and `sql.*` properties
- [ ] 9.3 Write `CsvTestDataLoader` `@Component` (test scope only) that reads `IZGW-FHIR-SamplePatientsData.csv` and `2019_10_01_imm.csv`, maps columns to the H2 schema, and bulk-inserts via `JdbcTemplate`
- [ ] 9.4 Verify H2 fixture loads cleanly for all 6,004 patient rows and associated immunization rows

## 10. Bulk FHIR Export — Kick-Off and Job Management

- [ ] 10.1 Create `BulkExportJob` model: `id` (UUID), `status` (PENDING/RUNNING/COMPLETE/FAILED), `kickoffTime`, `transactionTime`, `sinceParam`, `typeFilter`, `outputFiles` (list), `errorMessage`
- [ ] 10.2 Create `BulkExportJobManager` with `ConcurrentHashMap<UUID, BulkExportJob>`; methods: `create`, `get`, `markComplete`, `markFailed`, `delete`
- [ ] 10.3 Create `BulkExportController` `@RestController` at `/fhir/$export`
- [ ] 10.4 Implement `POST /$export` kick-off: validate `Accept` and `Prefer` headers, parse `_since`, `_type`, `_typeFilter` parameters, create job, return `202 Accepted` + `Content-Location` header
- [ ] 10.5 Implement `GET /fhir/$export-status/{jobId}` polling endpoint: return `202` (in-progress) or `200` with manifest JSON (complete) or `500` with `OperationOutcome` (failed)
- [ ] 10.6 Implement `DELETE /fhir/$export-status/{jobId}` completion notification: return `202 Accepted`, schedule temp file cleanup
- [ ] 10.7 Annotate all bulk export endpoints with appropriate `@RolesAllowed` (use existing `XFORM_SENDING_SYSTEM` role; add `BULK_EXPORT` role if needed per Open Question 1)

## 11. Bulk FHIR Export — NDJSON Generation

- [ ] 11.1 Create `BulkExportWorker` `@Async` service that runs export jobs in a background thread
- [ ] 11.2 Implement SQL query for bulk export: `SELECT * FROM immunizations [LEFT JOIN patients] WHERE INSERT_STAMP >= :since [AND INSERT_STAMP <= :until]`
- [ ] 11.3 Stream results in configurable chunks (default 10,000 rows per file) to temp NDJSON files using `TabularFhirConverter`
- [ ] 11.4 Implement `Patient` NDJSON generation: deduplicate patients across immunization rows; write one `Patient` line per unique patient
- [ ] 11.5 Implement `Immunization` NDJSON generation: one line per immunization row
- [ ] 11.6 Implement NDJSON file serving endpoint `GET /fhir/$export-files/{jobId}/{filename}` with `Content-Type: application/ndjson`
- [ ] 11.7 Update `BulkExportJobManager.markComplete()` to build the manifest JSON with `output` array (type, url, count per file)
- [ ] 11.8 Write integration test: kick off export on H2 fixture, poll to completion, download and validate NDJSON line count matches expected patient/immunization counts

## 12. Wiring, Configuration, and Documentation

- [ ] 12.1 Register `BulkExportController` with `AccessControlRegistry`
- [ ] 12.2 Add OpenAPI/Swagger annotations to `BulkExportController` and `SqlFhirBackend` endpoints
- [ ] 12.3 Update `testing/configuration/pipelines.json` to include an example `"sql"` destination entry
- [ ] 12.4 Document all new `sql.*` configuration properties in `README.md` or `docs/`
- [ ] 12.5 Confirm `openspec status --change wa-doh-sql-backend` shows all artifacts complete and commit
