## Purpose

Enable the Transformation Service to connect to an ANSI SQL database via JDBC.
SQL-backed queries are handled by `SqlFhirController` and `BulkExportController`
in `izgw-transform-sql`, which own distinct URL paths. All existing IZ Gateway
routing through `FhirController` is unchanged.

## Requirements

### Requirement: JDBC Datasource Configuration

The SQL back-end SHALL be configured exclusively through standard Spring Boot
datasource properties (`spring.datasource.*`) in `application.yml` or equivalent
external configuration.

#### Scenario: Datasource present — connector enabled

WHEN a valid `spring.datasource.url` is present in the application configuration  
THEN the `SqlBackendConnector` bean is instantiated and the `"sql"` destination is active

#### Scenario: Datasource absent — connector disabled

WHEN no `spring.datasource.url` is present in the application configuration  
THEN the `SqlBackendConnector` bean is NOT instantiated, no SQL-related endpoints are
registered, and any query routed to `"sql"` returns an appropriate 503 / feature-disabled error

#### Scenario: Datasource misconfigured

WHEN a `spring.datasource.url` is present but the connection cannot be established at startup  
THEN the application SHALL fail fast with a clear error message identifying the datasource
configuration problem

---

### Requirement: Reserved Path Prefix

`/sql/**` SHALL be a reserved URL path prefix. Multiple SQL backends may be active
simultaneously, each accessible at `/sql/fhir/{name}/**` (e.g., `/sql/fhir/waiis/`,
`/sql/fhir/prod/`, `/sql/fhir/dev/`). The built-in development fixture is `dev`,
accessible at `/sql/fhir/dev/**` with no configuration required.

#### Scenario: /sql/fhir path routed to SqlFhirController

WHEN a FHIR query arrives at `/sql/fhir/{name}/Patient` or similar  
THEN Spring MVC routes it to `SqlFhirController` in `izgw-transform-sql`  
AND the request never reaches `FhirController`

#### Scenario: Reserved path prefix protected from IIS registration

WHEN an attempt is made to register an IIS destination that would conflict with
the `/sql/**` path namespace  
THEN the registration SHALL be rejected with an informative error

---

### Requirement: Supported Database Dialects

The connector SHALL produce ANSI SQL-compatible queries that execute correctly on:
- Microsoft SQL Server (primary target: WA DOH Azure deployment)
- H2 (in-memory; used for automated testing)

#### Scenario: Query executes on SQL Server

WHEN the datasource URL points to a SQL Server instance  
AND the mapping configuration references a valid table  
THEN a patient query executes and returns results without dialect-specific errors

#### Scenario: Query executes on H2 in test profile

WHEN the `test` Spring profile is active  
AND the H2 in-memory datasource is configured  
THEN patient and immunization queries execute successfully against H2

---

### Requirement: Development Fixture — `dev` CSV Backend

The built-in `dev` backend (accessible at `/sql/fhir/dev/**`) SHALL provide a fully
functional mock backend requiring no JDBC driver and no embedded database. At startup
it loads the existing test data files into in-memory `List<Map<String,String>>` structures:
- `IZGW-FHIR-SamplePatientsData.csv` (6,004 patient records)
- `izgw-hub/testing/testdata/2019_10_01_imm.csv` (immunization records)

Patient search is implemented as a Java stream filter; immunization retrieval is a
second stream filter on patient ID. No SQL engine, no schema file, no JDBC dependency.

#### Scenario: Integration test uses dev fixture

WHEN an integration test sends a query to `/sql/fhir/dev/Patient`  
THEN the full query-match-retrieve-convert pipeline executes end-to-end using
the CSV-backed in-memory data, with no live SQL Server connection required

#### Scenario: dev backend active with no datasource configured

WHEN no `spring.datasource.url` is present  
THEN the `dev` backend at `/sql/fhir/dev/**` remains available (it does not use
Spring's datasource infrastructure)  
AND all other `/sql/fhir/{name}/**` backends are disabled
