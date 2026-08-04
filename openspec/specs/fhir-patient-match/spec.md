# fhir-patient-match Specification

## Purpose

This capability defines how the IZ Gateway Transformation Service handles FHIR Patient `$match` responses, including the too-many-matches OperationOutcome response shape required for interoperability with DIBBs Query Connector while preserving existing match, search, read, and transformation-pipeline behavior.

## Requirements

### Requirement: Too-many-matches responds with a top-level OperationOutcome

When the `POST /fhir/{destinationId}/Patient/$match` operation's converted IIS response contains zero Patient resources AND an OperationOutcome issue whose `details.coding` includes a coding with system `http://terminology.hl7.org/CodeSystem/v2-0208` and code `TM` (case-insensitive), the service SHALL respond with a top-level `OperationOutcome` resource (not a Bundle) and HTTP status 422 Unprocessable Entity.

The returned OperationOutcome SHALL contain an issue with:
- `severity` = `warning`
- `code` = `multiple-matches`
- `details.text` containing the exact substring `did not find a certain match` (the full text SHALL be "The matching operation found one or more possible matches, but did not find a certain match.")
- `details.coding` retaining the v2-0208 `TM` coding from the source query-status issue

The `details.text` substring `did not find a certain match` SHALL NOT be reworded — it is the literal trigger DIBBs Query Connector v1.2.0 matches on.

#### Scenario: IIS returns Z33 too-much-data-found

- **WHEN** a `$match` request is submitted and the IIS responds with a Z33 message whose QAK-2 is `TM` and which contains no patient records
- **THEN** the HTTP response body's top-level `resourceType` is `OperationOutcome`
- **AND** some `issue[].details.text` contains `did not find a certain match`
- **AND** some `issue[].details.coding[]` has system `http://terminology.hl7.org/CodeSystem/v2-0208` and code `TM`
- **AND** the issue has `severity` = `warning` and `code` = `multiple-matches`
- **AND** the HTTP status is 422

#### Scenario: DIBBs Query Connector recognizes the outcome

- **WHEN** DIBBs Query Connector v1.2.0 receives the too-many-matches `$match` response
- **THEN** its `noCertainMatch` body check evaluates true and the UI shows "No Certain Match Found" (not "No Records Found")

#### Scenario: Content type is negotiated from the request

- **WHEN** the too-many-matches `$match` response is produced for a request with an Accept header (e.g. `application/fhir+json` or `application/fhir+xml`)
- **THEN** the OperationOutcome is serialized in the negotiated content type, using the same header negotiation as the Bundle-returning `$match` path

### Requirement: All other $match outcomes are unchanged

The `$match` operation SHALL respond exactly as it does today for every outcome other than the too-many-matches case: a `searchset` Bundle of scored Patient resources with HTTP 200 when candidates are found, an empty `searchset` Bundle with HTTP 200 for a true no-match (QAK-2 `NF`), and the existing error handling (fault/exception OperationOutcomes with their current statuses) for IIS or processing errors. The reshape SHALL NOT trigger when any Patient resource is present in the converted response, regardless of query status codings.

#### Scenario: Candidates within the record limit

- **WHEN** a `$match` request yields one or more candidate patients within the IIS record limit
- **THEN** the response is a `searchset` Bundle with the Patient entries scored per the IDI Match algorithm (`entry.search.score` and `entry.search.mode` = `match`) and HTTP status 200

#### Scenario: Single certain match

- **WHEN** a `$match` request yields exactly one matching patient
- **THEN** the response is the unchanged `searchset` Bundle containing that scored Patient and HTTP status 200

#### Scenario: True no-match is distinguishable from too-many

- **WHEN** a `$match` request yields a response with QAK-2 `NF` (no data found) and zero patient records
- **THEN** the response remains a `searchset` Bundle (HTTP 200) with no Patient entries, so DIBBs shows "No Records Found"
- **AND** the top-level OperationOutcome reshape does not trigger

#### Scenario: Patients present alongside a TM status

- **WHEN** a converted `$match` response contains one or more Patient resources even though a `TM` query-status coding is present
- **THEN** the response is the `searchset` Bundle of scored Patients (HTTP 200); the reshape SHALL NOT discard returned candidates

### Requirement: Search and read paths are unaffected

The too-many-matches reshape applies to the `$match` operation only. GET searches, POST `_search` searches, and read interactions on `/fhir/{destinationId}/Patient`, `/fhir/{destinationId}/Immunization`, and `/fhir/{destinationId}/ImmunizationRecommendation` SHALL continue to return their current response shapes for all query statuses, including `TM`.

#### Scenario: GET Patient search with a TM response

- **WHEN** a `GET /fhir/{destinationId}/Patient?...` search yields an IIS response with QAK-2 `TM`
- **THEN** the response remains the current `searchset` Bundle containing the outcome entries, with HTTP status 200

### Requirement: Transformation pipeline behavior is preserved

The `$match` reshape SHALL operate only on the FHIR representation after the SOAP round-trip completes. The underlying request-direction and response-direction transformation pipelines (including reversed pipe execution on the response direction) SHALL execute exactly as before, and no Organization/Pipeline/Solution/Operation/Precondition configuration change SHALL be required for existing organizations.

#### Scenario: Existing org configurations require no change

- **WHEN** the service is deployed with existing organization pipeline configurations (file or DynamoDB backend)
- **THEN** `$match` requests continue to flow through the same pipelines in both directions, and the too-many-matches reshape occurs downstream of the response-direction pipes without any configuration migration
