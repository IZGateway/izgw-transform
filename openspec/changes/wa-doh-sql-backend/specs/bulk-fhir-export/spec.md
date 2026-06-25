## Purpose

Provide a full asynchronous Bulk FHIR `$export` endpoint conformant with the
HL7 Bulk Data Access specification, enabling WA DOH (and any authorized consumer)
to retrieve all immunization records from the SQL back-end as NDJSON, with
optional filtering by record creation dateTime or dateTime range.

In this deployment, `[base]` resolves to `/bulk/sql/fhir` — so `POST [base]/$export`
is `POST /bulk/sql/fhir/$export`, and status/file URLs follow the same prefix. The
`/bulk/{backend}/fhir/` structure accommodates future backends (e.g., `/bulk/izgw/fhir/`)
without path conflicts.

The minimum patient demographic requirements that apply to single-patient queries
(name, DOB, etc.) do NOT apply to `$export`. Bulk export is a population-level
operation driven solely by temporal filters and resource type selection.

## Requirements

### Requirement: BulkExportJobStore Interface

A `BulkExportJobStore` interface SHALL abstract all job state persistence so that
the V1 in-memory implementation and a future V2 DynamoDB implementation are
interchangeable with no business logic changes.

Methods: `create(BulkExportJob)`, `get(UUID)`, `update(BulkExportJob)`, `delete(UUID)`.

V1 implementation: `InMemoryBulkExportJobStore` using `ConcurrentHashMap<UUID, BulkExportJob>`.
V2 implementation: DynamoDB-backed (consistent with existing repository layer).

#### Scenario: Job state survives across ALB-routed requests (V2)

WHEN a kick-off request is handled by instance A  
AND a subsequent status poll or DELETE is routed by the ALB to instance B  
THEN instance B retrieves the job state from the shared store and responds correctly

#### Scenario: V1 single-instance limitation documented

WHEN the V1 in-memory store is active  
THEN job state is lost on restart and is not visible across instances  
AND this limitation is documented in the deployment guide

---

### Requirement: BulkExportOutputStore Interface

A `BulkExportOutputStore` interface SHALL abstract all NDJSON output file storage
so that V1 local temp files and a future V2 S3-backed store are interchangeable.

Methods: `write(UUID jobId, int fileIndex, InputStream data)`,
`stream(UUID jobId, int fileIndex, OutputStream out)`, `delete(UUID jobId)`.

V1 implementation: `TempFileBulkExportOutputStore` using `java.io.tmpdir`.
V2 implementation: S3 bucket accessible only via VPC Gateway Endpoint; downloads
are always proxied through the service — S3 is never exposed directly to callers,
preserving mTLS access control end-to-end.

#### Scenario: NDJSON download served regardless of which instance handles request (V2)

WHEN NDJSON files are stored in S3  
AND a download request is routed to any service instance  
THEN that instance fetches from S3 and streams to the authenticated client

#### Scenario: V1 temp file limitation documented

WHEN the V1 temp file store is active  
THEN output files are local to the instance that ran the export job  
AND requests for those files must reach that same instance (single-instance
deployment required for V1)

---

### Requirement: Kick-Off Request

The service SHALL expose a `POST [base]/$export` endpoint that accepts a Bulk FHIR
kick-off request per the HL7 Bulk Data Access specification.

#### Scenario: Valid kick-off accepted

WHEN a client sends a valid `POST /$export` with `Accept: application/fhir+json`
and `Prefer: respond-async` headers  
THEN the server responds with `202 Accepted`  
AND the response includes a `Content-Location` header pointing to a job status URL

#### Scenario: _since parameter accepted

WHEN the kick-off request includes `_since=<FHIR dateTime>`  
THEN the export job filters to include only records with `INSERT_STAMP >= _since`

#### Scenario: _typeFilter with date range accepted

WHEN the kick-off request includes a `_typeFilter` parameter encoding a dateTime range  
THEN the export job filters to records whose `INSERT_STAMP` falls within that range

#### Scenario: Missing Prefer header rejected

WHEN a client sends `POST /$export` without the `Prefer: respond-async` header  
THEN the server responds with `400 Bad Request`

---

### Requirement: Job Status Polling

The service SHALL expose a `GET [Content-Location URL]` endpoint for clients to
poll export job status.

#### Scenario: Job in progress

WHEN the export job has not completed  
THEN the polling endpoint returns `202 Accepted` with an optional `X-Progress` header

#### Scenario: Job complete

WHEN the export job has completed successfully  
THEN the polling endpoint returns `200 OK` with a JSON manifest body containing:
- `transactionTime` (FHIR instant)
- `request` (original kick-off URL)
- `requiresAccessToken` (boolean)
- `output` array with one entry per NDJSON file, each containing `type`, `url`, and `count`
- `error` array (empty on success)

#### Scenario: Job failed

WHEN the export job encounters an unrecoverable error  
THEN the polling endpoint returns `500` with an `OperationOutcome` in the body

---

### Requirement: NDJSON File Download

Each output URL in the manifest SHALL be downloadable as a file of NDJSON, with
one FHIR resource per line, per the Bulk FHIR specification.

#### Scenario: Patient NDJSON file served

WHEN a client downloads the URL for resource type `Patient`  
THEN the response body is NDJSON where each line is a valid FHIR Patient resource  
AND `Content-Type: application/ndjson` is set

#### Scenario: Immunization NDJSON file served

WHEN a client downloads the URL for resource type `Immunization`  
THEN the response body is NDJSON where each line is a valid FHIR Immunization resource

---

### Requirement: Job Completion Notification — DELETE

The service SHALL expose a `DELETE [Content-Location URL]` endpoint for clients to
signal they have finished downloading the export output.

#### Scenario: Delete accepted after download complete

WHEN a client sends `DELETE` to the job status URL after downloading output files  
THEN the server responds with `202 Accepted`  
AND the job's temporary output files are eligible for cleanup

---

### Requirement: DateTime Filtering

Export jobs SHALL support both `_since` (open-ended from a point in time) and a
closed dateTime range (from + to).

#### Scenario: _since filters by insertion timestamp

WHEN `_since` is provided  
THEN only immunization rows where `INSERT_STAMP >= _since` are included in the export

#### Scenario: Date range filters by insertion timestamp

WHEN both a `from` and `to` datetime are provided  
THEN only rows where `INSERT_STAMP >= from AND INSERT_STAMP <= to` are included

#### Scenario: No date filter returns all records

WHEN no `_since` or range parameter is provided  
THEN all non-deleted immunization records are included in the export

---

### Requirement: Resource Types Exported

The default export SHALL produce `Patient` and `Immunization` resources.

#### Scenario: _type parameter restricts resource types

WHEN the kick-off includes `_type=Immunization`  
THEN only `Immunization` NDJSON is produced (no Patient file)

#### Scenario: Default includes both types

WHEN no `_type` parameter is supplied  
THEN both `Patient` and `Immunization` NDJSON files are produced

---

### Requirement: Per-Type Filtering via `_typeFilter`

The kick-off endpoint SHALL accept the `_typeFilter` parameter per the HL7 Bulk
Data Access IG. Each value is a URL-encoded FHIR search expression scoped to a
single resource type (e.g., `Immunization?_lastUpdated=ge2024-01-01`). Filters
SHALL be applied as SQL WHERE clause predicates on the appropriate table — not
as post-filters in Java.

#### Scenario: `_typeFilter` applies temporal filter to Immunization

WHEN the kick-off includes `_typeFilter=Immunization%3F_lastUpdated%3Dge2024-01-01`  
THEN the immunization SQL query includes a WHERE predicate on the `is_last_updated`
column for that date  
AND Patient records are not filtered by that predicate

#### Scenario: `_typeFilter` and `_since` both present

WHEN both `_since` and a `_typeFilter` with `_lastUpdated` are present for the
same resource type  
THEN the more restrictive of the two bounds is applied (i.e., the later lower bound)

#### Scenario: Unrecognised `_typeFilter` parameter

WHEN a `_typeFilter` expression references a search parameter not supported by
the SQL backend  
THEN the server responds with `400 Bad Request` identifying the unsupported parameter
