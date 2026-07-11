# IZ Gateway Transformation Service — SQL-Backed FHIR Interface

*Part of the [IZ Gateway FHIR Interface](../fhir/index.md) documentation.*

This module (`izgw-transform-sql`) adds two FHIR API surfaces that query an ANSI
SQL database directly rather than routing through the IZ Gateway Hub and IIS.

> **Availability:** These endpoints only exist when the service is built with the
> `sql-support` Maven profile (`mvn package -P sql-support,sql-mssql`) and deployed
> as `xform-service-sql-dev` (or equivalent). The standard APHL deployment image does
> not include them. Requests to `/sql/**` or `/bulk/sql/**` paths on a standard build
> return `503 Service Unavailable`.

---

## Relationship to the Standard FHIR Interface

The SQL-backed API is a companion to — not a replacement for — the standard
hub-routed FHIR interface documented in [`docs/fhir/`](../fhir/):

> **Standard FHIR interface** (hub-routed, always available):
>
> - Endpoints: `GET /fhir/{destinationId}/Patient`, `Immunization`, `ImmunizationRecommendation`, etc.
> - Converts FHIR queries to HL7 V2 QBP, sends through IZ Gateway Hub, converts RSP response back to FHIR.
> - Supports all configured IIS destinations.

The SQL-backed interface follows the same FHIR R4 search conventions (parameters,
response format, mTLS authentication, roles) as the standard interface. The key
differences are the URL prefix and the data source:

| | Standard (hub-routed) | SQL-backed |
|---|---|---|
| Base URL | `/fhir/{destinationId}/` | `/sql/fhir/{name}/` |
| Bulk export | Not available | `/bulk/sql/fhir/$export` |
| Data source | IIS via IZ Gateway Hub | ANSI SQL database via JDBC |
| Backend config | Hub destination registration | `sql.backends.{name}` property |
| Image | Standard `transformation-service` | SQL-enabled `transformation-service-sql` |

---

## Documents

| Document | Description |
|---|---|
| [SQL FHIR API Reference](sql-fhir-api.md) | Single-patient query endpoints at `/sql/fhir/{name}/**` |
| [Bulk FHIR Export API](bulk-fhir-api.md) | Async `$export` endpoints at `/bulk/sql/fhir/$export` |
| [Local Testing Guide](local-testing.md) | Running the SQL engine locally with Docker; test endpoint configuration |

---

## How It Works

### Single-Patient Query

The request flows through three backend types depending on configuration:

```
FHIR Client
    |  GET /sql/fhir/dev/Patient?family=FagenAIRA&birthdate=1963-12-26
    v
SqlFhirController (izgw-transform-sql)
    |
    +-- "dev" backend (DEV_CSV): stream-filter patients.csv + immunizations.csv
    |
    +-- "test" backend (CSV): stream-filter all_vax_event.csv (one denormalized file)
    |
    +-- "waiis" backend (JDBC): SELECT ... FROM all_vax_event WHERE last_name=? AND birth_date=?
    |
    v
SqlPatientSearchService
    |  IDI scoring -- singular match required (threshold: 0.95)
    |
    +-- No match  --> 200 OK, empty Bundle (total: 0)
    +-- Ambiguous --> 422 Unprocessable Entity, OperationOutcome
    +-- Match     -->
    v
SqlImmunizationRetrievalService
    |  fetch all immunization rows for matched patient
    v
TabularFhirConverter
    |  maps SQL columns -> FHIR Patient + Immunization
    |  using sql-mapping.yml (column-to-FHIR mapping file)
    v
FHIR Client receives Bundle (searchset)
```

### Bulk FHIR Export

```
FHIR Client  ->  POST /bulk/sql/fhir/$export  ->  202 Accepted + Content-Location
                     |
                     v
           BulkExportWorker (async)
                     |  SELECT * FROM immunization_view WHERE INSERT_STAMP >= :since
                     v
           NDJSON files (Patient + Immunization, chunked)
                     |
                     v
FHIR Client  ->  GET /bulk/sql/fhir/$export-status/{jobId}  ->  200 + manifest
FHIR Client  ->  GET /bulk/sql/fhir/$export-files/{jobId}/{n}  ->  NDJSON stream
FHIR Client  ->  DELETE /bulk/sql/fhir/$export-status/{jobId}  ->  202
```

---

## Authentication and Authorization

Same requirements as the standard interface:
- **mTLS** client certificate authentication
- Role **`XFORM_SENDING_SYSTEM`** or **`ADMIN`** required for single-patient queries
- Role **`XFORM_SENDING_SYSTEM`**, **`ADMIN`**, or **`BULK_EXPORT`** for bulk export

---

## Configuration

SQL backends are registered in `application-sql.yml` (or equivalent environment
variables). The built-in `dev` and `test` backends require no configuration.
Additional JDBC backends are registered by name:

```yaml
sql:
  matching-threshold: 0.95   # IDI score threshold; 1 match required above this value

  backends:
    # built-in -- always available, no configuration needed
    dev:
      type: DEV_CSV
      patients-path: classpath:sql-dev/patients.csv
      immunizations-path: classpath:sql-dev/immunizations.csv
      mapping-config-path: classpath:sql-mapping.yml

    test:
      type: CSV
      data-path: ${SQL_BACKENDS_TEST_DATA_PATH:/data/all_vax_event.csv}
      mapping-config-path: ${SQL_BACKENDS_TEST_MAPPING_CONFIG_PATH:classpath:sql-mapping-wadoh.yml}

    # example JDBC backend -- accessible at /sql/fhir/waiis/**
    waiis:
      type: JDBC
      immunization-table: all_vax_event
      patient-id-column: ASIIS_PAT_ID
      mapping-config-path: file:/data/sql-mapping-wadoh.yml
      # DataSource: spring.datasource.url / username / password
```

See [`docs/sql-fhir/local-testing.md`](local-testing.md) for the full environment
variable reference, and
[`izgw-transform-sql/docs/wa-doh-pilot/`](https://github.com/IZGateway/izgw-transform-sql/tree/develop/docs/wa-doh-pilot/)
for WA DOH schema reference materials.
