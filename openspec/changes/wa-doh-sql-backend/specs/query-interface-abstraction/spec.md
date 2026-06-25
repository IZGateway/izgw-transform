## Purpose

Define how SQL-backed FHIR query endpoints are separated from the existing
hub-routed `FhirController` via Spring MVC path routing, keeping `izgw-core`
clean and eliminating any circular Maven dependency between `izgw-transform`
and `izgw-transform-sql`.

## Requirements

### Requirement: SqlFhirController Owns sql-* URL Paths

A `SqlFhirController` `@RestController` in `izgw-transform-sql` SHALL own all
FHIR query paths under `/sql/fhir/{name}/**`, where `{name}` is the SQL backend
name (e.g., `dev`, `waiis`). This path prefix is entirely distinct from
`/fhir/**` — there is no overlap and no ambiguity. `FhirController` in
`izgw-transform` is not modified.

#### Scenario: sql path routes to SqlFhirController

WHEN a FHIR query arrives at `/sql/fhir/dev/Patient`  
THEN Spring MVC routes it to `SqlFhirController`  
AND `FhirController` is never invoked for this request

#### Scenario: /fhir path routes to FhirController unchanged

WHEN a FHIR query arrives at `/fhir/izgateway/Patient`  
THEN Spring MVC routes it to `FhirController` exactly as before  
AND no SQL code is involved in handling the request

---

### Requirement: BulkExportController Owns /bulk/sql/fhir/ Paths

A `BulkExportController` `@RestController` in `izgw-transform-sql` SHALL own all
Bulk FHIR paths under `/bulk/sql/fhir/`: `POST /bulk/sql/fhir/$export`,
`GET /bulk/sql/fhir/$export-status/{jobId}`,
`DELETE /bulk/sql/fhir/$export-status/{jobId}`, and
`GET /bulk/sql/fhir/$export-files/{jobId}/{fileIndex}`. The `/bulk/{backend}/fhir/`
prefix separates bulk capability from single-patient query capability, accommodating
future backends (e.g., `/bulk/izgw/fhir/$export`). These paths are fully distinct
from `/fhir/**` and `/sql/fhir/**`.

#### Scenario: Bulk export kickoff handled independently

WHEN a client sends `POST /bulk/sql/fhir/$export`  
THEN `BulkExportController` handles the request independently of `FhirController`  
AND the hub path is never consulted

---

### Requirement: Stub Controller When SQL Not Configured

When `izgw-transform-sql` is not on the classpath (standard APHL build),
a stub controller SHALL be registered that returns `503 Service Unavailable`
with an `OperationOutcome` for all `/sql/**` and `/bulk/sql/**` paths.

#### Scenario: /sql path returns 503 without SQL module

WHEN `izgw-transform-sql` is not on the classpath  
AND a request arrives at `/sql/fhir/dev/Patient`  
THEN the stub controller returns `503 Service Unavailable` with an `OperationOutcome`  
AND the response body identifies that the SQL backend is not configured

#### Scenario: /bulk/sql path returns 503 without SQL module

WHEN `izgw-transform-sql` is not on the classpath  
AND a request arrives at `POST /bulk/sql/fhir/$export`  
THEN the stub controller returns `503 Service Unavailable`

---

### Requirement: No Shared Interface Between Modules

`izgw-transform-sql` SHALL depend only on `izgw-core` and Spring. It SHALL NOT
depend on `izgw-transform` at compile time. `FhirController` SHALL NOT import
any type from `izgw-transform-sql`. The `IQueryBackend` interface is internal
to `izgw-transform-sql` and is not shared.

#### Scenario: izgw-transform builds without izgw-transform-sql on classpath

WHEN the `sql-support` Maven profile is not active  
THEN `izgw-transform` compiles and tests pass without `izgw-transform-sql`  
AND no ClassNotFoundException occurs at runtime for sql-* paths (stub handles them)

#### Scenario: izgw-transform-sql builds without depending on izgw-transform

WHEN `izgw-transform-sql` is compiled  
THEN no import in `izgw-transform-sql` references a type from `izgw-transform`  
AND the Maven dependency graph contains no cycle
