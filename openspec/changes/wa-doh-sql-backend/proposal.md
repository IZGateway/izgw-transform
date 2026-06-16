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
Transformation Service to support any ANSI SQL database as a back-end while
preserving full compatibility with the existing IZ Gateway path.

## What Changes

- **New**: A SQL back-end connector module integrates with any ANSI SQL database
  (initially WA DOH WAIIS on Azure SQL Server) via a JDBC datasource configured
  through Spring Boot. If no datasource is configured the feature is silently disabled.
- **New**: `"sql"` is added as a reserved destination name. Queries routed to `"sql"`
  are handled by the SQL back-end connector rather than dispatched to IZ Gateway.
- **New**: The query data currently passed between front-end and back-end is
  formalized as a proper Java interface / POJO (`IQueryRequest` and friends), so
  neither side carries a direct dependency on the other's implementation.
- **Modified**: The IDIMatch patient-matching algorithm is generalized from operating
  on a concrete class to accepting an `IPatientMatchData` interface, enabling it to
  evaluate matches from any data source including SQL result rows.
- **New**: A SQL-backed patient search runs a parameterized ANSI SQL query against the
  configured database table using IDIMatch semantics to find a single best-match patient.
  A singular match is required before immunization records are retrieved.
- **New**: A SQL-backed immunization retrieval executes a second query to return all
  immunization rows for the confirmed patient ID.
- **New**: A configuration-driven tabular-to-FHIR converter maps database columns to
  FHIR resource locations using FHIRPath expressions. The configuration also performs
  concept mapping from raw database values to FHIR `string`, `number`, `code`,
  `Coding`, or `CodeableConcept` datatypes.
- **New**: The converted FHIR Bundle is returned through the existing Transformation
  Service response path, indistinguishable from a Bundle sourced from IZ Gateway.
- **New**: A Bulk FHIR `$export` endpoint supports full asynchronous export per the
  HL7 Bulk FHIR specification (kickoff → polling → NDJSON download → DELETE). Queries
  accept `_since` (records created after a dateTime) and optionally a dateTime range.
- **New**: An H2 in-memory database test fixture, pre-loaded from the existing
  `IZGW-FHIR-SamplePatientsData.csv` and `2019_10_01_imm.csv` test data files,
  provides a mock SQL back-end for integration tests.

## Capabilities

### New Capabilities

- `sql-backend-connector`: Spring Boot JDBC datasource integration, `"sql"` reserved
  destination routing, enable/disable via configuration presence
- `query-interface-abstraction`: Formal `IQueryRequest` interface / POJO decoupling
  Transformation Service front-end from back-end implementations
- `patient-matching-sql`: Generalized IDIMatch operating on `IPatientMatchData`;
  SQL patient search query; singular-match enforcement before proceeding
- `immunization-retrieval-sql`: Parameterized SQL query for all immunization rows
  belonging to a confirmed patient ID
- `tabular-fhir-conversion`: YAML configuration mapping database columns to FHIRPath
  destinations with optional concept mapping to FHIR primitive/complex types
- `bulk-fhir-export`: Full async HL7 Bulk FHIR `$export` endpoint with `_since` /
  range dateTime filtering, NDJSON output, job polling, and manifest generation

### Modified Capabilities

- `patient-matching` (IDIMatch): Requires generalization of matching algorithm to
  accept `IPatientMatchData` interface in place of current concrete parameter class.

## Impact

- **Repository**: `izgw-transform` (all changes)
- **New Spring dependencies**: `spring-boot-starter-data-jdbc`, H2 (test scope),
  SQL Server JDBC driver (runtime scope, optional)
- **New configuration**: `application.yml` datasource block; `sql-mapping.yml`
  column→FHIR mapping configuration
- **Existing IZ Gateway path**: Unchanged. SQL path is additive only.
- **Routing layer**: Destination `"sql"` intercepted before IZ Gateway dispatch
- **IDIMatch**: Interface extraction is a non-breaking refactor; existing callers
  continue to work
- **Test data**: `IZGW-FHIR-SamplePatientsData.csv` (6,004 patients) and
  `izgw-hub/testing/testdata/2019_10_01_imm.csv` loaded into H2 for integration tests
- **Bulk FHIR**: New REST endpoints; no existing endpoint changes
