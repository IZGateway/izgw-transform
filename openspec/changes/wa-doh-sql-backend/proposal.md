## User Story

As a WA DOH user, I want to be able to query my back-end IIS database as if it were a
FHIR Server supporting query and Bulk FHIR, so that I can use it to help me populate
my real FHIR back-end repository.

**Success Criteria:**

GIVEN I have deployed the IZ Gateway Transformation Service and connected it to my
database with the appropriate configuration parameters  
WHEN I query it using FHIR APIs or Bulk FHIR query  
THEN I get back responses with data already in FHIR format

---

## Why

The IZ Gateway Transformation Service today routes all queries to IZ Gateway as
its sole back-end. Washington State DOH (WA DOH) requires two additional capabilities
that IZ Gateway alone cannot provide: direct single-patient immunization queries
against the WAIIS IIS database (replacing time-consuming one-by-one manual lookups
during outbreak investigations), and bulk FHIR export of the full immunization
population to populate their Aidbox FHIR repository. This change generalizes the
Transformation Service to support any ANSI SQL-compatible database as a back-end —
including SQL Server, PostgreSQL, MySQL, AWS RDS families, Azure SQL, and any
other JDBC-accessible source — while preserving full compatibility with the existing
IZ Gateway path.

## What Changes

- **New**: A separate `izgw-transform-sql` module integrates with any ANSI
  SQL-compatible database via a standard JDBC datasource. The specific JDBC driver is
  deployment-supplied (SQL Server, PostgreSQL, MySQL, AWS RDS, Azure SQL, etc.). When
  the module is not on the classpath, all `/sql/**` paths return `503 Service
  Unavailable`; the existing `/fhir/**` paths are unaffected.
- **New**: SQL-backed FHIR queries are served at `/sql/fhir/{name}/**`, where `{name}`
  is the configured backend name (e.g., `dev`, `waiis`). These paths are entirely
  distinct from the existing `/fhir/**` paths. `FhirController` is not modified.
- **Modified**: `IDIMatch` patient matching is preserved unchanged. A new
  `SqlPatientRowMapper` converts SQL result rows (`Map<String,Object>`) to HAPI FHIR
  `Patient` objects, which are then scored by the existing IDIMatch algorithm.
- **New**: A SQL-backed patient search runs a parameterized ANSI SQL query against the
  configured database table using IDIMatch semantics to find a single best-match patient.
  A singular match is required before immunization records are retrieved.
- **New**: A SQL-backed immunization retrieval executes a second query to return all
  immunization rows for the confirmed patient ID.
- **New**: A configuration-driven tabular-to-FHIR converter maps database columns to
  FHIR resource locations using FHIRPath expressions. The configuration also performs
  concept mapping from raw database values to FHIR `string`, `number`, `code`,
  `Coding`, or `CodeableConcept` datatypes.
- **New**: A Bulk FHIR `$export` endpoint at `/bulk/sql/fhir/$export` supports full
  asynchronous export per the HL7 Bulk FHIR specification (kickoff → polling → NDJSON
  download → DELETE). Queries accept `_since` and optionally a dateTime range. The
  `/bulk/{backend}/fhir/` path prefix separates bulk capability from single-patient
  query capability, leaving room for future backends (e.g., `/bulk/izgw/fhir/$export`).
- **New**: A lightweight embedded SQL fixture, pre-loaded from the existing
  `IZGW-FHIR-SamplePatientsData.csv` and `2019_10_01_imm.csv` test data files,
  provides a mock SQL back-end accessible at `/sql/fhir/dev/**` for integration tests
  and local dev evaluation with no real database required.

## Capabilities

### New Capabilities

- `sql-backend-connector`: Spring Boot JDBC datasource integration in `izgw-transform-sql`;
  SQL endpoints at `/sql/fhir/{name}/**`; stub returns 503 when module absent
- `query-interface-abstraction`: Spring MVC path routing separating SQL endpoints
  (`/sql/fhir/**`) from hub endpoints (`/fhir/**`); `SqlPatientRowMapper` for SQL row →
  FHIR Patient conversion; `FhirController` unchanged
- `patient-matching-sql`: SQL patient search query using existing IDIMatch scoring via
  `SqlPatientRowMapper`; singular-match enforcement before proceeding
- `immunization-retrieval-sql`: Parameterized SQL query for all immunization rows
  belonging to a confirmed patient ID
- `tabular-fhir-conversion`: YAML configuration mapping database columns to FHIRPath
  destinations with optional concept mapping to FHIR primitive/complex types
- `bulk-fhir-export`: Full async HL7 Bulk FHIR `$export` at `/bulk/sql/fhir/$export`
  with `_since` / range dateTime filtering, NDJSON output, job polling, and manifest
  generation; path prefix `/bulk/{backend}/fhir/` supports future backends

### Modified Capabilities

- `patient-matching` (IDIMatch): No changes to IDIMatch; `SqlPatientRowMapper` bridges
  SQL rows to existing FHIR Patient-based matching.

## Impact

- **Repositories**: `izgw-transform` (profile dependency, path protection); new `izgw-transform-sql` module (all SQL backend code, JDBC, new controllers)
- **New Spring dependencies**: `spring-boot-starter-jdbc` and JDBC driver profiles in `izgw-transform-sql`; no changes to `izgw-core` or `izgw-transform` dependencies
- **New configuration**: `sql.backend.<name>` properties (or equivalent `SQL_BACKEND_<NAME>` env vars) pointing to per-backend YAML config files; `dev` backend built-in, no config needed (accessible at `/sql/fhir/dev/**`)
- **Existing IZ Gateway path**: Unchanged. `/fhir/**` paths and `FhirController` are not modified.
- **Routing**: Spring MVC path routing — `/sql/fhir/{name}/**` to `SqlFhirController`; `/bulk/sql/fhir/$export/**` to `BulkExportController`; `/fhir/**` to existing `FhirController`; future `/bulk/izgw/fhir/**` remains available as a path prefix
- **IDIMatch**: Unchanged; `SqlPatientRowMapper` bridges SQL rows to existing FHIR Patient-based matching
- **Test data**: `IZGW-FHIR-SamplePatientsData.csv` (6,004 patients) and `izgw-hub/testing/testdata/2019_10_01_imm.csv` loaded into embedded dev fixture at `/sql/fhir/dev/**`
- **Bulk FHIR**: New REST endpoints at `/sql/fhir/$export/**`; no changes to existing endpoints
