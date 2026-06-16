## Purpose

Enable the Transformation Service to connect to an ANSI SQL database via JDBC,
routing queries addressed to the reserved destination `"sql"` through that connection
while leaving all existing IZ Gateway routing unchanged.

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

### Requirement: Reserved Destination Name

`"sql"` SHALL be a reserved destination name in the Transformation Service routing layer.

#### Scenario: Query routed to sql destination

WHEN a query arrives with destination `"sql"`  
THEN it is dispatched to the `SqlBackendConnector` and NOT forwarded to IZ Gateway

#### Scenario: Reserved name protected from IIS registration

WHEN an attempt is made to register an IIS destination with the name `"sql"`  
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

### Requirement: Test Fixture — H2 Mock Backend

An H2 in-memory database SHALL be provided as a test fixture, pre-loaded from the
existing test data files:
- `IZGW-FHIR-SamplePatientsData.csv` (6,004 patient records)
- `izgw-hub/testing/testdata/2019_10_01_imm.csv` (immunization records)

The schema SHALL match the column names referenced in the default mapping configuration.

#### Scenario: Integration test uses H2 fixture

WHEN an integration test activates the `test` Spring profile  
THEN the H2 datasource is auto-configured, test data is loaded, and the full
query-match-retrieve-convert pipeline executes end-to-end without requiring a
live SQL Server connection
