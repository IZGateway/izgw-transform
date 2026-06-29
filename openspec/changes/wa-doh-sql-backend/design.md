## Context

The IZ Gateway Transformation Service (`izgw-transform`) is a Spring Boot application
that converts FHIR queries into HL7 V2 messages and dispatches them to IIS back-ends
via IZ Gateway. The FHIR entry point is `FhirController` (`/fhir`), which converts
inbound FHIR parameters into a `QBP_Q11` query, sends it through `HubController` →
`IZGHubProducer` (an Apache Camel producer), receives a V2 response, and converts it
back to a FHIR Bundle using the `v2tofhir` library.

`IDIMatch` is a stateless utility class (all static methods) that scores HAPI FHIR
`Patient` objects against a search `Patient` using the HL7 IDI patient-matching
algorithm. It already operates on FHIR types — not on raw SQL rows or custom POJOs.

The proposed SQL back-end sits beside the existing IZ Gateway path at a distinct
URL prefix (`/sql/fhir/**`). The existing `/fhir/**` path and `FhirController` are
not modified. Both paths return FHIR Bundles; the difference is the URL, the
underlying data source, and the controller that handles the request.

---

## Goals / Non-Goals

**Goals:**
- Enable single-patient FHIR queries against a JDBC-connected SQL database
- Enable full async Bulk FHIR `$export` from the same SQL database
- Preserve 100% backward compatibility with the existing IZ Gateway path
- Require no code changes when no SQL datasource is configured
- Support multiple simultaneous SQL backends (one per `sql-*` destination)
- Allow backends to be added or reconfigured without redeployment or code change
- Provide a working integration test and local dev fixture requiring no JDBC driver
- Support any ANSI SQL-compatible database via a deployer-supplied JDBC driver

**Non-Goals:**
- Replacing the Apache Camel routing layer for the IZ Gateway path
- Implementing a full FHIR server on top of the SQL database
- Paginated single-patient query results (bulk handles population-scale queries)
- OAuth / SMART on FHIR scoping for the Bulk FHIR endpoint (v1)
- Documenting EFS or other mount strategies for externalized config (v1)

---

## Decisions

### 1. Routing: dedicated controllers at distinct URL paths, not Camel and not FhirController dispatch

**Decision**: SQL-backed endpoints are served by dedicated Spring MVC controllers in
`izgw-transform-sql` at completely distinct URL paths. `FhirController` at `/fhir/**`
is not modified. `SqlFhirController` owns `/sql/fhir/{name}/**`; `BulkExportController`
owns `/sql/fhir/$export/**`. There is no path overlap and no shared routing logic.

**Rationale**: The SQL path produces a FHIR Bundle directly — there is no HL7 V2
message involved. Two alternatives were rejected:

- **Camel `SqlComponent`**: Camel's exchange model is oriented toward message bytes, not
  typed Java objects. The SQL path has no use for Camel's transformation or retry
  features.
- **FhirController dispatch via shared interface**: Any shared interface between
  `FhirController` (in `izgw-transform`) and SQL backends (in `izgw-transform-sql`)
  either pollutes `izgw-core` with FHIR types or creates a circular Maven dependency.
  Path routing eliminates the need for a shared interface entirely.

---

### 2. IDIMatch does not need a new interface

**Decision**: Do not change `IDIMatch`'s method signatures. Instead, introduce a
`SqlPatientRowMapper` that converts a SQL result row (`Map<String, Object>`) into a
HAPI FHIR `Patient` object. Pass those `Patient` objects to the existing IDIMatch
scoring methods.

**Rationale**: `IDIMatch` already operates on FHIR `Patient` objects, which are a
well-defined model. Building a `Patient` from a SQL row is a straightforward field
mapping. Creating an `IPatientMatchData` interface would duplicate the FHIR `Patient`
API and provide no benefit.

**How the SQL patient search works**:
1. The inbound FHIR `$match` or `Patient?` parameters are already in a FHIR `Patient`
   (the search patient) — this is the query input.
2. `SqlPatientSearchService` executes a broad SQL query (matching on name + DOB, or
   DOB + gender, etc.) returning candidate rows.
3. Each candidate row is mapped to a FHIR `Patient` by `SqlPatientRowMapper`.
4. IDIMatch scores each candidate `Patient` against the search `Patient`.
5. The one candidate at or above threshold (singular match required) has its patient ID
   extracted for immunization retrieval.

---

### 3. Routing: Spring MVC path routing, not backend selection inside FhirController

**Decision**: SQL-backed endpoints are served by dedicated Spring MVC controllers in
`izgw-transform-sql` that own distinct URL paths. `FhirController` in `izgw-transform`
is not modified.

- **`SqlFhirController`** owns `/sql/fhir/{name}/**` — mirrors `FhirController`'s
  query and read mappings for SQL backends, where `{name}` is the backend name
  (e.g., `dev`, `waiis`). Fully distinct from `/fhir/**`; no path overlap.
- **`BulkExportController`** owns `/bulk/sql/fhir/$export/**` — the full async Bulk
  FHIR export lifecycle (kickoff, polling, NDJSON download, DELETE). The
  `/bulk/{backend}/fhir/` prefix separates bulk capability from single-patient query
  capability and leaves room for future backends (e.g., `/bulk/izgw/fhir/$export`).
- **`SqlUnavailableController`** (stub, activated when SQL module is absent) returns
  `503 Service Unavailable` with an `OperationOutcome` for all `/sql/**` and
  `/bulk/sql/**` paths so callers receive a meaningful error rather than a 404.

`IQueryBackend` is an internal interface within `izgw-transform-sql`. It is not shared
with `izgw-transform` or `izgw-core`. `izgw-transform-sql` depends only on `izgw-core`
— there is no circular Maven dependency.

**Rationale**: Any approach where `FhirController` dispatches to SQL backends requires
a shared interface between `izgw-transform` and `izgw-transform-sql`. That shared
interface must live in `izgw-core` (polluting it with FHIR types) or in a new module.
Spring MVC path routing eliminates the need for a shared interface entirely: each
controller owns its URL space, and the SQL module is simply loaded onto the classpath
via the `sql-support` Maven profile with no compile-time coupling from
`izgw-transform` to `izgw-transform-sql`.

**Alternative considered and rejected**: Modifying `FhirController` to inject
`List<IQueryBackend>` and select by `supports(destinationId)`. Rejected because
`IQueryBackend` would need to be defined outside `izgw-transform-sql` (circular
dependency if defined there, `izgw-core` pollution if defined there) and because
`FhirController` is already correct for hub routing and should not be entangled with
SQL concerns.

---

### 4. Tabular-to-FHIR conversion: FHIRPath-keyed YAML config

**Decision**: Define a YAML configuration (`sql-mapping.yml`) where each entry specifies:
- `column`: database column name (case-insensitive)
- `resource`: FHIR resource type (`Patient` or `Immunization`)
- `path`: FHIRPath expression relative to the resource root (e.g., `name.family`)
- `type`: FHIR datatype (`string`, `date`, `code`, `Coding`, `CodeableConcept`, etc.)
- `system` (optional): code system URI for Coding/CodeableConcept
- `concept_map` (optional): list of `{from, to}` pairs

Use the HAPI FHIR `FhirPathEngine` to resolve the target path on the resource and
set the value. For simple primitive paths, direct setter calls are acceptable as an
optimization for common types (name.family, birthDate, etc.).

**Rationale**: FHIRPath provides a standard, extensible way to address any element in
any FHIR resource without hard-coding getter/setter chains per column. The WA DOH
`all_vax_event` schema is fully covered by this mechanism using the enriched mapping
from `all_vax_event_enriched_mapping.csv`.

**Alternative considered**: Pure annotation-based Java mapping class. Rejected because
it requires redeployment for every schema change. The YAML config supports hot-reload.

---

### 5. Bulk FHIR job storage: interface-abstracted, in-memory for v1

**Decision**: Define two interfaces:

- `BulkExportJobStore` — `create / get / update / delete` job state keyed by job UUID
- `BulkExportOutputStore` — `write(jobId, fileIndex, InputStream) / stream(jobId, fileIndex, OutputStream) / delete(jobId)`

V1 implements both with in-memory / `java.io.tmpdir`. V2 implements with DynamoDB
(job state) + S3 (NDJSON output). No business logic changes between versions.

**V1 behavior**: `ConcurrentHashMap` for job state; `java.io.tmpdir` for NDJSON files.
Suitable for WA DOH's single-instance deployment. Jobs and files are lost on restart;
document this limitation (WA DOH can re-kick exports).

**V2 target (multi-instance / restart-safe)**:
- Job state → DynamoDB table (consistent with existing repository layer; atomic
  conditional writes prevent duplicate job execution across instances)
- NDJSON output → S3 bucket (VPC Gateway Endpoint only — traffic never leaves AWS,
  bucket policy denies all non-VPC access)
- Download URLs in the manifest point back to **the service endpoint**
  (e.g., `GET /fhir/$export/jobs/{id}/files/{n}`), not to S3 directly. The service
  fetches from S3 and streams to the authenticated client. S3 is never exposed to
  callers; mTLS access control is preserved end-to-end.

**Why not S3 pre-signed URLs**: Pre-signed URLs bypass mTLS client certificate
authentication and expose PHI over the public internet to anyone with the URL.
Routing downloads through the service keeps all access control at the service layer.

---

### 6. Reserved path prefix

**Decision**: Reserve `/sql/**` as a protected URL path prefix. The access control
layer rejects any attempt to register an IIS destination that would conflict with this
namespace. The `dev` backend is the built-in embedded fixture, accessible at
`/sql/fhir/dev/**` with no configuration required.

---

### 7. View-based query model: one-table denormalized or two-table normalized

**Decision**: Support both a one-table denormalized model and a two-table normalized
model via the same ANSI SQL query patterns. The model is selected per backend via
`sql.backends.{name}.patient-table` and `sql.backends.{name}.immunization-table`.
When both point to the same table it is denormalized; when they differ it is normalized.

For a **normalized** backend (two distinct views):
```sql
SELECT DISTINCT <patient_columns> FROM patient_view  WHERE last_name = :lastName AND birth_date = :dob
SELECT * FROM immunization_view WHERE patient_id = :patientId
```

For a **denormalized** backend (single all\_vax\_event-style table):
```sql
SELECT DISTINCT <patient_columns> FROM all_vax_event WHERE last_name = :lastName AND birth_date = :dob
SELECT * FROM all_vax_event WHERE patient_id = :patientId
```

The `SELECT DISTINCT <patient_columns>` projection is derived at startup from the
`Patient` resource entries in `sql-mapping.yml` -- not hardcoded. This ensures the
DISTINCT deduplicates on demographic columns only, not on immunization-specific columns
that vary per row. All parameter values are bound as named parameters; column names come
from the trusted `sql-mapping.yml` (never from user input).

**Rationale**: WA DOH's `all_vax_event` is a single denormalized analytical table
produced by a Databricks notebook joining ~15 WAIIS source tables. Its 61 columns
cover both demographics and immunization events. Using one table for both queries
avoids requiring WA DOH to create two views when one table already exists. The
`SELECT DISTINCT` approach naturally deduplicates patients without any extra
application-level logic.

**WA DOH source table**: `tc_iis_prod.analytic_tables.all_vax_event`. Its 61 columns
are defined in `all_vax_event_enriched_mapping.csv` (reference materials folder).
The `sql-mapping.yml` is generated from this file and reviewed before use -- see
tasks 2.3a-2.3b. The `INSERT_STAMP` timestamp column is the `is_last_updated`
anchor for both `_lastUpdated` filtering and `_since` in bulk export.

---

### 8. CSV-backed backends: `dev` (two-file) and `test` (one-file)

Two CSV-backed `IQueryBackend` implementations provide local testing without any
database or JDBC driver.

**`SqlDevBackend`** (accessible at `/sql/fhir/dev/**`): loads two separate CSV files
in hub test data format -- one patient file, one immunization file with a patient ID
foreign key. Patient search is a Java stream filter; immunization retrieval is a
second stream filter on patient ID. Files are bundled in the classpath by default.

**`SqlTestBackend`** (accessible at `/sql/fhir/test/**`): loads a single denormalized
CSV file in the `all_vax_event` column format. Each row carries both patient demographics
and one immunization event. Patient search filters rows by demographics and deduplicates
by patient ID; immunization retrieval returns all rows for the matched patient ID.
The file path defaults to `/data/all_vax_event.csv` and is overridable via
`SQL_BACKENDS_TEST_DATA_PATH`, enabling engineers to mount their own CSV extract into
a running container without an image rebuild.

Both backends implement `IQueryBackend` and are registered by `SqlBackendAutoConfiguration`
from the `sql.backends` config block. The `test` backend is always registered when the
SQL module is active; it returns a meaningful 503 if the data file is absent.

**Rationale**: Eliminating the embedded database removes ~2.5 MB from the image and
all CVE surface associated with an embedded engine. The `test` endpoint gives WA DOH
the ability to validate a CSV extract locally before connecting to the production
SQL Server instance.

---

### 9. Backend discovery: `sql.backends.{name}` config block

**Decision**: Each SQL backend is declared as a structured config block under
`sql.backends.{name}`, where each block carries its own type, data/table paths, and
mapping config path. Spring Boot's relaxed binding makes environment variable overrides
natural -- e.g., `SQL_BACKENDS_TEST_DATA_PATH` overrides `sql.backends.test.data-path`.

```yaml
sql:
  matching-threshold: 0.95
  backends:
    dev:
      type: DEV_CSV
      patients-path: classpath:sql-dev/patients.csv
      immunizations-path: classpath:sql-dev/immunizations.csv
      mapping-config-path: classpath:sql-mapping.yml
    test:
      type: CSV
      data-path: ${SQL_BACKENDS_TEST_DATA_PATH:/data/all_vax_event.csv}
      mapping-config-path: ${SQL_BACKENDS_TEST_MAPPING_CONFIG_PATH:classpath:sql-mapping.yml}
    wa-doh:
      type: JDBC
      patient-table: all_vax_event
      immunization-table: all_vax_event
      patient-id-column: IIS_PATIENT_ID
      mapping-config-path: /data/sql-mapping.yml
```

`SqlBackendAutoConfiguration` reads this map at startup and creates the appropriate
`IQueryBackend` instance for each entry. The `dev` backend is always registered with
built-in classpath defaults if not explicitly configured. The `test` backend is
registered when the SQL module is active. JDBC backends are registered only when a
`DataSource` bean is present.

**Rationale**: Grouping all backend configuration under a single named block (rather
than a path-only property) makes each backend self-describing -- type, data source,
and column mapping are co-located. This simplifies validation, logging, and future
extension (e.g., adding connection pool settings to a JDBC block). The `dev` and `test`
backends ship with sensible defaults requiring zero configuration for local testing.

---

### 10. JDBC driver packaging

**Decision**: All supported JDBC drivers are declared in `izgw-transform/pom.xml`
(not `izgw-bom`) and activated via Maven profiles:

| Profile | Driver |
|---|---|
| `sql-mssql` | `com.microsoft.sqlserver:mssql-jdbc` |
| `sql-postgres` | `org.postgresql:postgresql` |
| `sql-mysql` | `com.mysql:mysql-connector-j` |
| `sql-oracle` | `com.oracle.database.jdbc:ojdbc11` |

The base image (e.g., APHL) is built with no SQL profile — zero SQL driver JARs.
Deployment-specific images activate the appropriate profile(s):
```
mvn package -P sql-mssql   # WA DOH image
```
Driver versions are pinned in `izgw-transform/pom.xml`. A deployer needing an
unsupported driver may supply it via `loader.path` (documented as an advanced option;
they own CVE responsibility for externally supplied JARs).

---

### 11. SQL Server integration test target: persistent AWS RDS Express instance

**Decision**: SQL Server integration tests run against a persistent AWS RDS SQL Server
Express instance shared across CI runs. Test data is loaded once at instance creation;
all CI test runs are read-only thereafter. The instance runs 24/7 — no stop/start
scheduling. At ~$20/month for a `db.t3.micro`, the cost is low enough that the
engineering time to automate scheduling would never pay back.

A Maven profile `sql-server-it` activates `mssql-jdbc` and a `@Tag("sql-server")`
JUnit category so SQL Server tests are skipped in standard local and PR builds and only
run when the profile is explicitly activated (e.g., in a nightly or release CI job that
can reach the RDS instance).

**Rationale**: No H2 is needed. Unit tests for `SqlPatientSearchService` and
`SqlImmunizationRetrievalService` mock `JdbcTemplate` directly — the queries are too
simple (single-predicate `SELECT *`) to warrant a SQL engine at the unit test level.
The `sql-dev` CSV fixture covers fast end-to-end smoke testing in PRs. The RDS instance
provides the real SQL Server dialect test for release confidence. WA DOH's own Azure
SQL Server deployment is the final production validation.

---

### 12. Local test capability: self-signed BCFKS keystore and JWT token generator

**Decision**: The SQL-enabled Docker image bundles two local-testing aids that require
no external infrastructure:

1. **Self-signed BCFKS keystore** generated at image build time via `keytool -genkeypair`
   using the existing `bc-fips-2.1.2.jar`. CN follows the IZ Gateway local-test
   convention: `CN=sql.xform.testing.local, O=izgateway`. Stored at `/ssl/local/`.
   Engineers pass `XFORM_CRYPTO_STORE_KEY_TOMCAT_SERVER_FILE=/ssl/local/server.bcfks`
   and `COMMON_PASS=changeit` to use it. Production deployments supply their own
   keystore via the same env vars -- the bundled cert is never used in production.

2. **`generate-token` Docker command**: running `docker run <image> generate-token`
   invokes a Node.js script (built-in `crypto` only) that prints two signed JWTs:
   a sender token (`xform-sender` role) and an admin token (`xform-sender` + `admin`).
   Both are signed with `XFORM_JWT_SECRET` (base64-encoded HMAC-SHA256 key). The
   engineer generates the secret once with `openssl rand -base64 32`, passes it to
   both the token generator and the running container, and uses the token as a
   `Bearer` header.

The `test` endpoint (`/sql/fhir/test/**`) is always registered when the SQL module is
active. It reads from the CSV file mounted at `/data/all_vax_event.csv` (overridable
via env var). Engineers mount their own CSV extract at that path.

**Rationale**: mTLS client certificates are the primary access control mechanism in
production, but they require PKI infrastructure (issuing CA, signed client certs) that
is impractical for a one-day local evaluation. JWT shared-secret auth is already
supported in `izgw-core` (`JwtSharedSecretPrincipalProvider`) and is activated by
setting a single env var. The self-signed BCFKS keystore eliminates the need for an
EFS-mounted keystore for local runs. Together these reduce the "time to first query"
for an engineer from days to minutes.

---

## Risks / Trade-offs

| Risk | Mitigation |
|---|---|
| SQL injection via mapping config | All query parameters bound via `JdbcTemplate` named parameters; column names come from config (not user input), validated at startup |
| H2 dialect incompatibility with SQL Server | Use ANSI SQL-only features in `SqlPatientSearchService`; test with both dialects in CI |
| IDI match threshold too aggressive (many no-matches) | Default threshold configurable; document recommended values for WA DOH dataset |
| In-memory Bulk FHIR jobs lost on restart | V1: document; WA DOH's use case is scheduled batch runs. V2: DynamoDB + S3 (see Decision 5) |
| Multi-instance load-balanced deployment breaks in-memory job state | V1: single-instance only. V2: DynamoDB job store + S3 output store resolves this (see Decision 5) |
| Large NDJSON files blocking temp storage | Apply configurable max-rows-per-file; default to chunked output (e.g., 10,000 rows/file) |
| FHIRPath evaluation performance for large exports | Pre-resolve common paths to direct setter calls; profile with 6,000-row test dataset |

---

## Migration Plan

1. **No migration required for existing deployments** — the SQL back-end activates
   only when `spring.datasource.url` is present. Existing deployments without
   datasource config are unaffected.
2. **WA DOH deployment**: Add datasource config block + `sql-mapping.yml` to
   `application.yml`. No restart of existing IZ Gateway connectivity required.
3. **Rollback**: Remove `spring.datasource.*` from config and restart. SQL endpoints
   become 503. No data is affected.

---

## Open Questions

1. **Authentication for Bulk FHIR**: Should `$export` require a specific role beyond
   `XFORM_SENDING_SYSTEM`? WA DOH may need a dedicated bulk-export role.
2. **Patient ID namespace**: What system URI should be used for the `ASIIS_PAT_ID`
   identifier in the Patient resource? (e.g., `urn:oid:2.16.840.1.114222.4.1.xxx`)
3. **Chunked NDJSON file size**: Should the chunk size be configurable per deployment,
   or fixed at a well-tested default?
4. **`sql-mapping.yml` location in repo**: Ship a default WA DOH mapping in
   `src/main/resources/`; allow override via `sql.mapping.config-path` property.
5. **Table/column name configuration**: Does WA DOH need the table names to be
   configurable, or will the default names matching the H2 test fixture be sufficient?
