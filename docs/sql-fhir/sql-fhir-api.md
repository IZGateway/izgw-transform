# SQL FHIR API Reference

*Part of the [IZ Gateway SQL-Backed FHIR Interface](index.md) documentation.*

> **See also:** [Standard hub-routed FHIR API](../fhir/fhir-api.md)
> for the equivalent endpoints at `/fhir/{destinationId}/`. Query parameters,
> response formats, and FHIR operations are identical; only the base URL and data
> source differ.

> **Implementation status:** Stage 1 endpoints are live at `dev.sql-xform.izgateway.org`.
> Single-patient queries currently return an empty Bundle (patient matching and
> immunization retrieval are implemented in Stage 2). Bulk export is fully functional.

---

## Base URL

```
/sql/fhir/{name}/
```

`{name}` is the SQL backend name configured via `sql.backend.<name>` (e.g., `dev`,
`waiis`, `prod`). The built-in `dev` backend requires no configuration.

---

## Endpoints

### Search

```
GET  /sql/fhir/{name}/{ResourceType}?{params}
POST /sql/fhir/{name}/{ResourceType}/_search
HEAD /sql/fhir/{name}/{ResourceType}?{params}
```

Supported resource types: `Patient`, `Immunization`, `ImmunizationRecommendation`

#### Query Parameters

| Parameter | Type | Description |
|---|---|---|
| `family` | string | Patient family name (required for patient matching) |
| `birthdate` | date | Patient date of birth (`yyyy-MM-dd`) |
| `given` | string | Patient given name |
| `gender` | code | Patient gender (`male`, `female`, `unknown`) |
| `_lastUpdated` | date prefix | Filter by record insertion timestamp (server-side WHERE clause); supports `ge`, `le`, `gt`, `lt` prefixes and closed ranges |

Patient matching uses the IDI algorithm. **A singular match is required** before
immunization records are returned:

| Match result | Response |
|---|---|
| Exactly one candidate >= threshold | Bundle containing Patient + Immunization resources |
| No candidates above threshold | Empty Bundle (`total: 0`) |
| Two or more candidates above threshold | `OperationOutcome` (ambiguous match) |

#### `_lastUpdated` Filtering

`_lastUpdated` is applied as a SQL `WHERE` predicate on the `INSERT_STAMP` column
(or equivalent `is_last_updated` column from `sql-mapping.yml`) -- not as a
post-filter. This means only matching rows are fetched from the database.

```
GET /sql/fhir/waiis/Patient?family=Smith&birthdate=1985-03-15&_lastUpdated=ge2024-01-01
```

---

### Read

```
GET /sql/fhir/{name}/{ResourceType}/{id}
```

Fetches a previously returned resource by its stable FHIR ID. Internally performs
the same patient search and selects the matching resource from the result.

---

### Patient Match

```
POST /sql/fhir/{name}/Patient/$match
Content-Type: application/fhir+json

{ "resourceType": "Parameters", "parameter": [{ "name": "resource", "resource": { <Patient> } }] }
```

Accepts a `Patient` or `Parameters` resource body. Returns a Bundle of candidate
patients scored by the IDI matching algorithm.

---

## Response Format

All endpoints return `application/fhir+json` by default. Specify `Accept` header
for other formats:

- `application/fhir+json`
- `application/fhir+xml`
- `application/fhir+yaml`
- `application/json`, `application/xml`, `text/xml` (aliases)

---

## Column Mapping

The mapping from SQL columns to FHIR resource elements is defined in
`sql-mapping.yml` (classpath) or the path specified by `sql.mapping.config-path`.
The canonical mapping is generated from
[`docs/wa-doh-pilot/all_vax_event_enriched_mapping.csv`](../wa-doh-pilot/all_vax_event_enriched_mapping.csv).

The worked WA DOH example mapping is at
[`src/main/resources/sql-mapping-wadoh.yml`](https://github.com/IZGateway/izgw-transform-sql/blob/develop/src/main/resources/sql-mapping-wadoh.yml)
in the `izgw-transform-sql` repository.
