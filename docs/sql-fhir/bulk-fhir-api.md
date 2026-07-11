# Bulk FHIR Export API

*Part of the [IZ Gateway SQL-Backed FHIR Interface](index.md) documentation.*

Implements the [HL7 Bulk Data Access specification](https://hl7.org/fhir/uv/bulkdata/)
for asynchronous population-level export from the SQL backend.

> **No standard equivalent.** Bulk FHIR export has no counterpart in the standard
> hub-routed interface. See [sql-fhir-api.md](sql-fhir-api.md) for single-patient
> queries, and the [standard FHIR interface](../fhir/fhir-api.md)
> for hub-routed queries.

---

## Base URL

```
/bulk/sql/fhir/
```

---

## Lifecycle

```
1. POST  /bulk/sql/fhir/$export              → 202 Accepted + Content-Location header
2. GET   /bulk/sql/fhir/$export-status/{id}  → 202 (in progress) | 200 + manifest (complete)
3. GET   /bulk/sql/fhir/$export-files/{id}/{n} → NDJSON stream
4. DELETE /bulk/sql/fhir/$export-status/{id} → 202 (cleanup)
```

---

## Endpoints

### Kick Off Export

```
POST /bulk/sql/fhir/$export
Accept: application/fhir+json
Prefer: respond-async
```

**Required headers:**
- `Prefer: respond-async` — omitting this returns `400 Bad Request`

**Optional query parameters:**

| Parameter | Description |
|---|---|
| `_since` | Export only records where `INSERT_STAMP >= _since` (FHIR dateTime) |
| `_type` | Comma-separated resource types; default: `Patient,Immunization` |
| `_typeFilter` | FHIR search expression per type, e.g. `Immunization?_lastUpdated=ge2024-01-01`; applied as SQL WHERE clause |

**Response:**
```
HTTP/1.1 202 Accepted
Content-Location: /bulk/sql/fhir/$export-status/{jobId}
```

`_typeFilter` parameters that reference unsupported search parameters return
`400 Bad Request`. Supported: `_lastUpdated` on `Patient` and `Immunization`.

---

### Poll Status

```
GET /bulk/sql/fhir/$export-status/{jobId}
```

| Job state | Response |
|---|---|
| In progress | `202 Accepted` + `X-Progress` header |
| Complete | `200 OK` + JSON manifest body |
| Failed | `500 Internal Server Error` + `OperationOutcome` |

**Manifest body (on completion):**
```json
{
  "transactionTime": "2024-06-26T14:00:00Z",
  "requiresAccessToken": true,
  "output": [
    { "type": "Patient",       "url": "/bulk/sql/fhir/$export-files/{id}/0", "count": 6004 },
    { "type": "Immunization",  "url": "/bulk/sql/fhir/$export-files/{id}/1", "count": 42317 }
  ],
  "error": []
}
```

---

### Download NDJSON

```
GET /bulk/sql/fhir/$export-files/{jobId}/{fileIndex}
```

Returns one FHIR resource per line in NDJSON format.

```
Content-Type: application/ndjson
```

Files are chunked at 10,000 rows by default (configurable). All downloads are
proxied through the service — S3 or temp storage is never exposed directly, and
mTLS access control is preserved end-to-end.

---

### Signal Completion

```
DELETE /bulk/sql/fhir/$export-status/{jobId}
```

Signals that the client has finished downloading all output files. The server
schedules cleanup of temporary files.

**Response:** `202 Accepted`

---

## Temporal Filtering

`_since` and `_typeFilter` both filter on the same `INSERT_STAMP` column (or the
column marked `is_last_updated: true` in `sql-mapping.yml`). When both are present
for the same resource type, the more restrictive (later) lower bound is applied.

The filter is applied **server-side as a SQL WHERE clause**, never as a post-filter
in Java. This ensures the database performs the scan rather than fetching the full
dataset.

---

## Resource Types

Default export produces `Patient` and `Immunization`. Each type is deduplicated:
- **Patient**: one record per unique patient in the immunization result set
- **Immunization**: one record per immunization row

Use `_type=Immunization` to suppress the Patient file.

---

## V1 Limitations

The current V1 implementation uses in-process job state and local temp files.

| Limitation | Workaround |
|---|---|
| Job state lost on restart | Re-kick the export; WA DOH's use case is scheduled batch runs |
| Single-instance only | V2 will use DynamoDB + S3 for multi-instance support |
| No SMART on FHIR scoping | Planned for V2 |
