_Integration tests follow the izgw-hub CI/CD pipeline pattern:_
_install → build → unit test → package → verify → push → verify in ECR → deploy → test._
_The Postman/Newman test stage runs post-deploy; all stages have failure criteria._
_Each stage below ends with a deploy checkpoint. No stage begins until the previous checkpoint passes._

---

## Stage 0 — AWS Infrastructure Planning

_No code. Output is a documented AWS configuration checklist before any deployment._
_See `stage0-aws-infrastructure-oracle-notes.md` for full interview record. AWS environment reference: `C:\Users\boonek\eclipse-workspace\izg-aws-environment.md`._

- [x] 0.1 Create a second ECS service in the existing `xform-service-alb-dev` cluster for the SQL-enabled image; base new task definition on `xform-service-alb-dev` but depart with SQL-specific env vars; create a new EFS access point on `fs-0c76fe796cfc1d1e8` at path `/dev/xform-service-sql` for SQL config and mapping files
- [x] 0.2 Add host-header listener rule to existing `xform-service-alb-dev` ALB routing `dev.sql-xform.izgateway.org` to a new target group for the SQL-enabled service; request ACM certificate for `dev.sql-xform.izgateway.org` (Amazon-issued); add as additional SNI cert on existing listener; WAF, mTLS trust store, and FIPS SSL policy are ALB-level and require no changes; create Route 53 record pointing `dev.sql-xform.izgateway.org` to the ALB
- [x] 0.3 Add custom policy to `izgateway-dev-izgateway-service` role granting `rds-db:connect` on the dev RDS instance ARN (future-proofing for non-SQL-Server backends; `SecretsManagerReadWrite` already covers SQL Server credential retrieval)
- [x] 0.4 Create dedicated RDS security group with single inbound rule: TCP 1433 from ECS task security group `sg-0673ad74bd304fe8f`; Fargate default outbound is sufficient for ECS side; document outbound port 1433 requirement in deployment guide for customers with restrictive egress policies
- [x] 0.5 Create Secrets Manager secret `xform-dev-sql-credentials` with key/value pairs `username` and `password`; `spring.datasource.url` is NOT a secret — it goes in the EFS-mounted SQL config file alongside the secret name `xform-dev-sql-credentials`; `SqlBackendProperties` will have a `credentialsSecret` field; `SqlBackendAutoConfiguration` retrieves and injects credentials programmatically at startup; `SecretsManagerReadWrite` already on shared task role — no additional IAM change needed for secret retrieval
- [x] 0.6 Document all of the above in `docs/aws-sql-deployment.md` as GFM Markdown chunks (same style as agent-skillz docs); scope is AWS dev/test RDS deployment only — Azure SQL Server deployment details deferred to a future CR; note in `izgw-transform-sql` repo conventions that documentation is delivered as GFM MD convertible to PDF/Word

---

## Stage 1 — Repository Setup and Endpoint Skeletons

_Goal: endpoints exist and respond correctly. No real data yet._
_`izgw-core` is untouched. `izgw-transform-sql` adds new URL paths via its own controllers — `FhirController` in `izgw-transform` is never modified. No circular dependency: `izgw-transform-sql` depends only on `izgw-core`._

_**Out of scope — tracked as new CR `fhir-capability-statement`:** FHIR metadata endpoint (`GET [base]/metadata`) is absent from both `FhirController` and `SqlFhirController`. Each controller needs a distinct `CapabilityStatement`. Do not add to this CR._

### 1A — izgw-core

_No changes. Not touched by this CR._

### 1B — izgw-transform-sql

- [x] 1.2 Create `izgw-transform-sql` GitHub repository; add `pom.xml` with parent `izgw-bom` and `spring-boot-starter-jdbc`; add `.project` file for Eclipse import ✅
- [x] 1.3 Add Maven CI/CD workflow following `izgw-core` library pattern: build, unit test, CVE scan, publish artifact to GitHub Packages on merge to `develop`
- [x] **1B.PR1** Open PR `IGDD-3013-wa-doh-sql-backend → develop` in `izgw-transform-sql` for CI/CD workflow only; merge before writing further code so all subsequent commits build under CI ✅ PR #1 merged
- [x] 1.4 Add `AutoConfiguration.imports` in `izgw-transform-sql` registering `SqlBackendAutoConfiguration`
- [x] 1.5 Create `SqlFhirController` `@RestController` at base path `/sql/fhir/{name}` — mirrors `FhirController`'s query and read mappings (`/Patient`, `/Immunization`, `/ImmunizationRecommendation`, `/{resource}/{id}`, `/Patient/$match`) but under `/sql/fhir/{name}/**`; for Stage 1, all query endpoints return an empty Bundle; register with `AccessControlRegistry`; add Swagger/OpenAPI annotations
- [x] 1.6 Create stub `SqlUnavailableController` `@RestController` covering `/sql/**` and `/bulk/sql/**` (activated via `@ConditionalOnMissingBean(SqlBackendAutoConfiguration.class)`) returning `503 Service Unavailable` with an `OperationOutcome` when SQL module is not configured
- [x] 1.7 Create `BulkExportJob` model: `id` (UUID), `status` (PENDING/RUNNING/COMPLETE/FAILED), `kickoffTime`, `transactionTime`, `sinceParam`, `typeFilter`, `outputFiles` (list), `errorMessage`
- [x] 1.8 Define `BulkExportJobStore` interface: `create(BulkExportJob)`, `get(UUID)`, `update(BulkExportJob)`, `delete(UUID)`
- [x] 1.9 Define `BulkExportOutputStore` interface: `write(UUID jobId, int fileIndex, InputStream data)`, `stream(UUID jobId, int fileIndex, OutputStream out)`, `delete(UUID jobId)`
- [x] 1.10 Implement `InMemoryBulkExportJobStore implements BulkExportJobStore` using `ConcurrentHashMap<UUID, BulkExportJob>` (V1); document single-instance limitation
- [x] 1.11 Implement `TempFileBulkExportOutputStore implements BulkExportOutputStore` using `java.io.tmpdir` (V1); document single-instance limitation
- [x] 1.12 Create `BulkExportController` `@RestController` at `/bulk/sql/fhir/$export`; inject `BulkExportJobStore` and `BulkExportOutputStore`; register with `AccessControlRegistry`; add Swagger/OpenAPI annotations
- [x] 1.13 Implement `POST /bulk/sql/fhir/$export` kick-off: validate `Accept` and `Prefer` headers; parse `_since`, `_type`, and `_typeFilter` parameters; validate supported `_typeFilter` parameters (return `400` if not); create job via `BulkExportJobStore`; return `202 Accepted` + `Content-Location` header
- [x] 1.14 Implement `GET /fhir/$export-status/{jobId}`: retrieve job via `BulkExportJobStore`; return `202` (in-progress), `200` with manifest JSON (complete), or `500` with `OperationOutcome` (failed)
- [x] 1.15 Implement `DELETE /fhir/$export-status/{jobId}`: return `202 Accepted`; call `BulkExportOutputStore.delete(jobId)` and `BulkExportJobStore.delete(jobId)`
- [x] 1.16 Annotate all endpoints with `@RolesAllowed` (use existing `XFORM_SENDING_SYSTEM` role; add `BULK_EXPORT` role if needed per Open Question 1)
- [x] 1.17 Unit tests for `SqlFhirController`: verify sql-* paths return 200 with empty Bundle; verify non-sql paths are not handled by this controller
- [x] 1.18 Unit tests for `BulkExportController`: verify `POST` returns 202 + `Content-Location`; verify missing `Prefer` header returns 400; verify unsupported `_typeFilter` returns 400; verify `GET` returns 202 in-progress; verify `DELETE` returns 202
- [x] **1B.PR2** Open PR `IGDD-3013-wa-doh-sql-backend → develop` in `izgw-transform-sql`; work until CI passes and PR is merged before starting 1C ✅ PR #2 merged

### 1C — izgw-transform

_`FhirController` is not modified. The only changes here are wiring the SQL module onto the classpath and protecting reserved destination names._

- [x] 1.19 Add `sql-support` Maven profile to `izgw-transform/pom.xml` with runtime-scoped dependency on `izgw-transform-sql` (same explicit version pattern as `izgw-core`)
- [x] 1.20 Register `/sql/**` as a protected path prefix in the access control layer so it cannot be hijacked by IIS destination registration; routing is now path-based rather than destination-name-based
- [x] 1.21 Verify existing unit and integration tests still pass with no changes to `FhirController`
- [x] **1C.PR** Open PR `IGDD-3013-wa-doh-sql-backend → develop` in `izgw-transform`; CI passes; existing tests green ✅ PR #271 passing
- [ ] 1.22 Create Postman collection `sql-backend.postman_collection.json` in `testing/scripts/` with Stage 1 smoke tests: flush log cache before each test; verify `GET /sql/fhir/dev/Patient` returns 200 with empty Bundle; verify `POST /bulk/sql/fhir/$export` returns 202; verify poll returns 202 in-progress; verify DELETE returns 202; verify `/fhir/izgateway/Patient` still routes correctly to hub; query log cache and assert no uncaught exceptions
- [ ] **1.DEPLOY** Deploy to dev; run Stage 1 Postman smoke tests; all pass before proceeding to Stage 2

---

## Stage 2 — sql-dev CSV Fixture and Mapping Infrastructure

_Goal: single-patient query against `sql-dev` returns real FHIR data._

- [x] 2.1 Create `SqlMappingConfig` model classes: `SqlMappingConfiguration` (top-level), `ResourceMapping` (per column with optional `is_last_updated: true` flag), `ConceptMapEntry` (`from`/`to`)
- [x] 2.2 Implement `SqlMappingConfigLoader` that loads `sql-mapping.yml` from classpath or external `sql.mapping.config-path`; use Jackson YAML or SnakeYAML
- [x] 2.3 Write the default `sql-mapping.yml` in `src/main/resources/` covering patient and immunization CSV columns; mark `Record Creation Date` as `is_last_updated: true` for both resources
- [x] 2.4 Create `SqlTableMapper<T>` abstract class in `izgw-transform-sql`; defines `T map(Map<String,Object> row, SqlMappingConfiguration config)` and owns all column-to-FHIR conversion logic shared across resource types
- [x] 2.5 Implement primitive type converters in `SqlTableMapper<T>`: `string`, `integer`, `decimal`, `boolean`, `date`, `dateTime`
- [x] 2.6 Implement complex type converters in `SqlTableMapper<T>`: `code`, `Coding` (with system), `CodeableConcept` (with system + optional display)
- [x] 2.7 Implement concept mapping lookup in `SqlTableMapper<T>` (`from` → `to` with optional default passthrough)
- [x] 2.8 Implement direct setter-based element assignment for all mapped paths; full FHIRPath engine deferred (not needed for known column set)
- [x] 2.9 Create `SqlPatientRowMapper extends SqlTableMapper<Patient>`; maps row columns to FHIR `Patient` demographics using mapping config
- [x] 2.10 Map patient demographics: first name, middle name, last name, DOB, gender, address (street1, street2, city, state, ZIP), phone, MRN/patient ID as `Identifier`
- [x] 2.11 Write unit tests for `SqlPatientRowMapper` covering each mapped field and null/missing column handling
- [x] 2.12 Create `SqlImmunizationRowMapper extends SqlTableMapper<Immunization>`; maps row columns to FHIR `Immunization` resource fields using mapping config
- [x] 2.13 Write unit tests for `SqlImmunizationRowMapper` covering each mapped field and null/missing column handling
- [x] 2.14 Create `TabularFhirConverter` that accepts `SqlPatientRowMapper` and `SqlImmunizationRowMapper`; delegates row conversion; assembles FHIR `Bundle` of type `searchset`; ensures single `Patient` resource per bundle
- [x] 2.15 Write unit tests for `TabularFhirConverter` verifying correct Bundle assembly
- [x] 2.16 Create `SqlDevBackend`: at startup loads CSV files into `List<Map<String,String>>`; patient search via Java stream filter; immunization retrieval via second stream filter; no JDBC
- [x] 2.17 Create `SqlBackendAutoConfiguration`; wire `SqlDevBackend` as always-available bean; `SqlMappingConfiguration` loaded from `sql-mapping.yml`
- [x] 2.18 Create `SqlBackendProperties` `@ConfigurationProperties(prefix = "sql")`
- [ ] 2.19 Add `application.yml` documentation comments for the new `sql.*` config block
- [ ] 2.20 Add Stage 2 Postman tests to collection: single-patient query against `sql-dev` with known test patient returns Bundle containing Patient and Immunization resources; verify log cache shows no exceptions; verify no-match and ambiguous-match response shapes
- [ ] **2.DEPLOY** Deploy to dev; run full Postman collection; Stage 1 smoke tests still pass; Stage 2 tests return real data from `sql-dev`

---

## Stage 3 — Full Single-Patient Query with Temporal Filtering

_Goal: real JDBC-backed query path and `_lastUpdated` filtering verified._

- [ ] 3.1 Create `SqlPatientSearchService` with `JdbcTemplate`; implement broad ANSI SQL candidate query using name + DOB OR DOB + gender; when `DateRangeParam lastUpdated` is non-null, append timestamp WHERE predicate against `is_last_updated` column from mapping config; all parameters bound via JDBC named parameters (no string concatenation)
- [ ] 3.2 Call `SqlPatientRowMapper` to convert each candidate row to a FHIR `Patient`
- [ ] 3.3 Call existing `IDIMatch` static scoring methods on each candidate `Patient` vs the search `Patient`
- [ ] 3.4 Implement singular-match enforcement: return matched patient ID if exactly one candidate ≥ threshold; return empty Bundle if zero; return ambiguous `OperationOutcome` if two or more
- [ ] 3.5 Write unit tests for `SqlPatientSearchService` using mocked `JdbcTemplate` returning synthetic `List<Map<String,Object>>` rows; verify correct SQL string, parameter binding, and IDIMatch handoff
- [ ] 3.6 Create `SqlImmunizationRetrievalService` with `JdbcTemplate`; implement parameterized ANSI SQL query for all immunization rows by patient ID; when `DateRangeParam lastUpdated` is non-null, append timestamp WHERE predicate against `is_last_updated` column for Immunization from mapping config
- [ ] 3.7 Return results as `List<Map<String,Object>>` with column names preserved
- [ ] 3.8 Write unit tests for `SqlImmunizationRetrievalService` using mocked `JdbcTemplate`; verify correct SQL and patient ID parameter binding
- [ ] 3.9 Wire `SqlFhirBackend` fully: `SqlPatientSearchService` → `SqlImmunizationRetrievalService` → `TabularFhirConverter`; return resulting Bundle through `FhirController` response serialization path
- [ ] 3.10 Add Stage 3 Postman tests: `_lastUpdated` temporal filtering on single-patient query; verify via log cache that WHERE clause is present in SQL; verify singular/no-match/ambiguous response shapes; verify `_lastUpdated` absent returns all records
- [ ] **3.DEPLOY** Deploy SQL-enabled image to dev with datasource configured; run full Postman collection; Stages 1–2 still pass; Stage 3 temporal filtering tests pass with log cache inspection confirming server-side predicate

---

## Stage 4 — Bulk Export

_Goal: full async bulk export lifecycle working end-to-end._

- [ ] 4.1 Create `BulkExportWorker` `@Async` service; inject `BulkExportJobStore` and `BulkExportOutputStore` (not concrete implementations)
- [ ] 4.2 Implement SQL query for bulk export: `SELECT * FROM immunizations [LEFT JOIN patients] WHERE INSERT_STAMP >= :since [AND INSERT_STAMP <= :until]`; apply `_typeFilter` predicates per resource type as additional WHERE clauses; apply the more restrictive bound when `_since` and a `_typeFilter _lastUpdated` overlap
- [ ] 4.3 Stream results in configurable chunks (default 10,000 rows per file) to NDJSON via `BulkExportOutputStore.write()` using `TabularFhirConverter`
- [ ] 4.4 Implement `Patient` NDJSON generation: deduplicate patients across immunization rows; write one `Patient` line per unique patient
- [ ] 4.5 Implement `Immunization` NDJSON generation: one line per immunization row
- [ ] 4.6 Implement NDJSON file serving endpoint `GET /fhir/$export-files/{jobId}/{fileIndex}` with `Content-Type: application/ndjson`; stream via `BulkExportOutputStore.stream()`
- [ ] 4.7 Update `BulkExportJobStore` job state via `update()` on completion to build manifest JSON with `output` array (type, url, count per file)
- [ ] 4.8 Add Stage 4 Postman tests: full bulk export lifecycle (kickoff → poll until complete → download each NDJSON file → verify line counts → DELETE); verify `_type=Immunization` omits Patient file; verify `_typeFilter` temporal filtering; verify `_since` alignment with single-patient `_lastUpdated`; log cache inspection throughout
- [ ] **4.DEPLOY** Deploy to dev; run full Postman collection; Stages 1–3 still pass; Stage 4 bulk export lifecycle passes; NDJSON line counts match expected from `sql-dev` fixture

---

## Stage 5 — SQL Server / RDS Integration

_Goal: full test suite passes against real SQL Server on AWS RDS._

- [ ] 5.1 Add Maven driver profiles to `izgw-transform-sql/pom.xml`: `sql-mssql` (`mssql-jdbc`), `sql-postgres` (`postgresql`), `sql-mysql` (`mysql-connector-j`), `sql-oracle` (`ojdbc11`); add `spring-boot-starter-validation` if not already present
- [ ] 5.2 Build SQL Server-enabled image: `mvn package -P sql-support,sql-mssql`; verify image builds cleanly; confirm no SQL-driver CVEs affect the APHL (base) image
- [ ] 5.3 Provision AWS RDS SQL Server Express test instance per Stage 0 checklist; load test data (6,004 patients + immunizations); confirm read-only access from ECS task role
- [ ] 5.4 Wire RDS credentials from Secrets Manager into ECS task definition for SQL-enabled service; deploy SQL Server-enabled image
- [ ] 5.5 Create Postman environment file `sql-rds.postman_environment.json` with RDS endpoint and known test patient records
- [ ] 5.6 Run full Postman collection against RDS SQL Server; verify all Stage 1–4 tests pass against real SQL Server dialect; confirm no dialect-specific errors
- [ ] 5.7 Create `sql-staging.postman_environment.json` and `sql-prod.postman_environment.json` with known test patient records for deployment verification
- [ ] **5.DEPLOY** SQL-enabled image deployed against RDS SQL Server; all Postman tests pass; no SQL Server dialect failures

---

## Stage 6 — Documentation and Completion

- [ ] 6.1 Update `testing/configuration/pipelines.json` to include an example `"sql-dev"` destination entry
- [ ] 6.2 Document all new `sql.*` configuration properties and `izgw-transform-sql` Maven profile options in `README.md` or `docs/`; reference `docs/aws-sql-deployment.md` from Stage 0; write with WA State DOH as the deployment audience
- [ ] 6.3 Configure Newman step in CI/CD test stage to run `sql-backend` collection against `sql-dev` environment on standard builds; run against `sql-staging` environment post-deploy to staging; run against RDS environment when deploying SQL-enabled image
- [ ] 6.4 Confirm `openspec status --change wa-doh-sql-backend` shows all artifacts complete and commit
