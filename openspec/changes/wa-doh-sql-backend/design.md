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

### 7. View-based query model

**Decision**: Require the deployer's database to expose exactly two views (names
configurable per backend):
- `patient_view` — one row per patient
- `immunization_view` — one row per immunization event, with a patient ID foreign key

Our queries are simple ANSI SQL:
```sql
SELECT * FROM patient_view  WHERE last_name = ? AND birth_date = ?
SELECT * FROM immunization_view WHERE patient_id = ?
```

**Rationale**: All schema complexity, underlying table joins, and indexing decisions
live inside the view definition — the deployer's concern. We never need to know the
underlying table structure. The resulting SQL is trivially portable across any ANSI
SQL-compatible database.

---

### 8. `sql-dev` fixture: CSV-backed in-memory, no JDBC driver

**Decision**: The `dev` backend (accessible at `/sql/fhir/dev/**`) does not use an
embedded SQL database. At startup it loads the existing `IZGW-FHIR-SamplePatientsData.csv`
and `2019_10_01_imm.csv` test data files into `List<Map<String,String>>`. Patient
search is a Java stream filter; immunization retrieval is a second filter on patient ID.
No SQL parser, no JDBC driver, no embedded database dependency.

**Rationale**: The view-based query model reduces our SQL to simple equality/range
predicates on a flat row. This is trivially reproducible with stream filtering.
Eliminating the embedded database removes ~2.5 MB from the image and all CVE surface
associated with an embedded engine.

---

### 9. Backend discovery: Spring property or environment variable (equivalent)

**Decision**: Each SQL backend is declared via a Spring property of the form:
```
sql.backend.<name>=/path/to/<name>.yml
```
Spring Boot's relaxed binding makes the env var form `SQL_BACKEND_<NAME>` exactly
equivalent — deployers may use either. At startup, `SqlBackendFactory` reads all
bound `sql.backend.*` entries and creates one `sql-<name>` backend per entry, loading
its config from the referenced file. `sql-dev` is built-in and requires no property.

**Examples** (both forms produce identical results):
```yaml
# application.yml
sql:
  backend:
    waiis: /opt/config/waiis.yml
    prod:  /opt/config/prod.yml
```
```
# environment variables
SQL_BACKEND_WAIIS=/opt/config/waiis.yml
SQL_BACKEND_PROD=/opt/config/prod.yml
```
Both register backends accessible at `/sql/fhir/waiis/**` and `/sql/fhir/prod/**`.

Adding a backend = add a property or env var, provide the config file, restart the
container task. No code change, no image rebuild.

**Rationale**: Spring's relaxed binding unifies both mechanisms for free. Deployers
choose the delivery strategy that fits their infrastructure (ECS task environment,
`application.yml` overlay, Kubernetes ConfigMap, `.env` locally) without any
difference in application behavior. V1 specifies only this convention; mount/delivery
strategy is a deployment concern.

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
