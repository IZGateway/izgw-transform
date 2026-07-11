# SQL FHIR API Reference

*Part of the [IZ Gateway SQL-Backed FHIR Interface](index.md) documentation.*

> **See also:** [Standard hub-routed FHIR API](../fhir/fhir-api.md)
> for the equivalent endpoints at `/fhir/{destinationId}/`. Query parameters,
> response formats, and FHIR operations are identical; only the base URL and data
> source differ.

> **Implementation status:** Stage 2 complete. Single-patient queries against the
> built-in `dev` and `test` backends return real FHIR data from the AIRA RSP test
> population (6,004 patients, 37,040 immunization events). Bulk export is fully
> functional. SQL Server / RDS backend (Stage 5) is in progress.

---

## Base URL

```
/sql/fhir/{name}/
```

`{name}` is the SQL backend name configured via `sql.backends.{name}` in
`application-sql.yml` (e.g., `dev`, `test`, `waiis`). The built-in `dev` and `test`
backends require no configuration and are always available.

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
| `_lastUpdated` | date prefix | Filter by record insertion timestamp; supports `ge`, `le`, `gt`, `lt` prefixes and closed ranges (see [Temporal Filtering](#lastUpdated-filtering)) |

---

### Patient Matching Behavior

Matching works in two stages:

1. **Candidate retrieval** -- the service issues a broad query (last name + date of birth)
   to retrieve all plausible candidates. For CSV backends this is an in-memory stream
   filter; for JDBC backends it is a parameterized SQL query with a `WHERE` clause.

2. **IDI scoring** -- each candidate is scored 0.0-1.0 against the request demographics
   using the IDI (Immunization Data Integration) matching algorithm. A score at or above
   `sql.matching-threshold` (default: **0.95**) qualifies as a match.

**A singular match is required** before immunization records are returned:

| Match result | HTTP | Response body |
|---|---|---|
| Exactly one candidate >= threshold | `200 OK` | `Bundle` (type: `searchset`) containing Patient and Immunization entries |
| No candidates >= threshold | `200 OK` | Empty `Bundle` (`total: 0`, no entries) |
| Two or more candidates >= threshold | `422 Unprocessable Entity` | `OperationOutcome` (code: `multiple-matches`) |

The threshold is configurable via `sql.matching-threshold`. Lowering it (e.g., `0.85`)
accepts fuzzier name spellings; raising it toward `1.0` requires a near-exact match.

#### Example: Singular Match

```
GET /sql/fhir/dev/Patient?family=FagenAIRA&birthdate=1963-12-26&_format=json
```

Response (`200 OK`):
```json
{
  "resourceType": "Bundle",
  "type": "searchset",
  "total": 1,
  "entry": [
    { "resource": { "resourceType": "Patient", ... } },
    { "resource": { "resourceType": "Immunization", ... } }
  ]
}
```

#### Example: No Match

```
GET /sql/fhir/dev/Patient?family=ZZZNOMATCH&birthdate=1900-01-01&_format=json
```

Response (`200 OK`):
```json
{
  "resourceType": "Bundle",
  "type": "searchset",
  "total": 0,
  "entry": []
}
```

#### Example: Ambiguous Match

Response (`422 Unprocessable Entity`):
```json
{
  "resourceType": "OperationOutcome",
  "issue": [{
    "severity": "error",
    "code": "multiple-matches",
    "diagnostics": "Patient query returned multiple candidates above the match threshold."
  }]
}
```

---

### `_lastUpdated` Filtering

`_lastUpdated` is applied as a server-side predicate on the column marked
`is_last_updated: true` in `sql-mapping.yml` (typically `INSERT_STAMP`). For CSV
backends this is a string comparison on the column value; for JDBC backends it
becomes a SQL `WHERE` clause. Only rows satisfying the predicate are returned --
it is not a post-filter.

Supported operators: `ge` (>=), `gt` (>), `le` (<=), `lt` (<). Use two parameters
for a closed range:

```
GET /sql/fhir/dev/Patient?family=FagenAIRA&birthdate=1963-12-26
    &_lastUpdated=ge2020-01-01&_lastUpdated=le2024-12-31&_format=json
```

If no column is marked `is_last_updated: true` in the mapping, the `_lastUpdated`
parameter is accepted but ignored (no filtering applied).

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

All endpoints return `application/fhir+json` by default. Specify the `Accept` header
for other formats:

- `application/fhir+json`
- `application/fhir+xml`
- `application/fhir+yaml`
- `application/json`, `application/xml`, `text/xml` (aliases)

---

## Column Mapping

The mapping from SQL columns to FHIR resource elements is defined in
`sql-mapping.yml` (classpath) or the path specified by `mapping-config-path` in the
backend configuration. Each backend can reference a different mapping file.

The canonical WA DOH mapping targets the `all_vax_event` view published by WAIIS:

- **Mapping file:** `src/main/resources/sql-mapping-wadoh.yml` in `izgw-transform-sql`
- **Source documentation:** `docs/sql-fhir/wa-doh-all-vax-event-mapping.csv` in `izgw-transform`

The enriched mapping CSV covers all 61 WA DOH `all_vax_event` columns: SQL column
name, FHIR target path, value type, concept maps (gender, race, ethnicity, route,
site, VFC eligibility), and which column anchors `_lastUpdated` filtering.

See [Local Testing Guide](local-testing.md) for the full list of key columns and
their FHIR mappings.
