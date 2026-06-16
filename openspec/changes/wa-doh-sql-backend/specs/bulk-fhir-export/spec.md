## Purpose

Provide a full asynchronous Bulk FHIR `$export` endpoint conformant with the
HL7 Bulk Data Access specification, enabling WA DOH (and any authorized consumer)
to retrieve all immunization records from the SQL back-end as NDJSON, with
optional filtering by record creation dateTime or dateTime range.

## Requirements

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
