# fhir-capability-statement Specification

## Purpose

This capability defines the FHIR R4 capabilities interaction exposed at `GET /fhir/{destinationId}/metadata`, which returns a valid `CapabilityStatement` resource describing the service's FHIR endpoints (Patient, Immunization, and ImmunizationRecommendation searches and the Patient `$match` operation) so that clients such as DIBBs Query Connector can auto-detect server capabilities.

## Requirements

### Requirement: FHIR CapabilityStatement conformance endpoint

The service SHALL expose the FHIR R4 capabilities interaction at `GET /fhir/{destinationId}/metadata`. A successful request MUST return HTTP `200` with a body that is a valid FHIR R4 `CapabilityStatement` resource. This endpoint is additive and MUST NOT alter the behavior, request/response contract, or status codes of any existing FHIR search or `$match` endpoint.

#### Scenario: Metadata request returns a CapabilityStatement

- **WHEN** an authorized client sends `GET /fhir/{destinationId}/metadata`
- **THEN** the service responds with HTTP `200`
- **AND** the response body is a FHIR `CapabilityStatement` resource (`resourceType`/`fhirType` = `CapabilityStatement`) with `status` = `active`

#### Scenario: Existing FHIR endpoints are unchanged

- **WHEN** the `/metadata` endpoint is added
- **THEN** the existing `GET /fhir/{destinationId}/Patient`, `/Immunization`, `/ImmunizationRecommendation` search endpoints and the `POST /fhir/{destinationId}/Patient/$match` operation SHALL continue to behave exactly as before, with no change to their responses

### Requirement: Declared FHIR version and formats

The `CapabilityStatement` SHALL declare `fhirVersion` = `4.0.1` and SHALL declare `application/fhir+json` in its `format` list, so that clients such as DIBBs Query Connector can read the FHIR version and negotiate JSON.

#### Scenario: FHIR version and JSON format are advertised

- **WHEN** a client reads the returned `CapabilityStatement`
- **THEN** `fhirVersion` equals `4.0.1`
- **AND** `format` includes `application/fhir+json`

### Requirement: R4 required elements and invariants

Beyond `status`, `fhirVersion`, and `format`, the `CapabilityStatement` SHALL populate the remaining elements FHIR R4 requires for a valid resource: `date` (1..1) and `kind` (1..1, value `instance` since this document describes a running server). Because `kind` = `instance`, the statement SHALL include an `implementation` element with a `description` (R4 invariant cpb-14), which also satisfies invariant cpb-2 (at least one of `description`, `software`, or `implementation`).

#### Scenario: Required elements are present

- **WHEN** a client reads the returned `CapabilityStatement`
- **THEN** `date` is populated
- **AND** `kind` equals `instance`
- **AND** `implementation.description` is populated

### Requirement: Response content type

The endpoint SHALL serialize the `CapabilityStatement` as FHIR JSON and return it with the `application/fhir+json` content type by default. Where a client requests an alternate representation supported by the existing FHIR controller (for example `application/fhir+xml`), the endpoint SHOULD honor that representation consistently with the other FHIR endpoints.

#### Scenario: Default JSON response

- **WHEN** a client sends `GET /fhir/{destinationId}/metadata` without a specific `Accept` header (or requesting JSON)
- **THEN** the response `Content-Type` is `application/fhir+json`
- **AND** the body is a JSON-serialized `CapabilityStatement`

### Requirement: Advertise supported FHIR resources and interactions

The `CapabilityStatement` MUST contain a single `rest` entry with `mode` = `server` that lists, at minimum, the `Patient`, `Immunization`, and `ImmunizationRecommendation` resource types, each declaring the `search-type` interaction.

#### Scenario: Queryable resources are listed with search-type

- **WHEN** a client reads `rest[0].resource` from the returned `CapabilityStatement`
- **THEN** it contains resource entries of type `Patient`, `Immunization`, and `ImmunizationRecommendation`
- **AND** each of those resources declares an `interaction` with `code` = `search-type`

### Requirement: Advertise the Patient $match operation

The `CapabilityStatement` MUST advertise the FHIR `Patient/$match` operation so that clients that auto-detect capabilities (for example DIBBs Query Connector) enable `$match`. It SHALL do so as a `Patient` resource-level `operation` whose `name` = `match` and whose `definition` = `http://hl7.org/fhir/OperationDefinition/Patient-match`.

#### Scenario: Patient resource advertises the match operation

- **WHEN** a client inspects the `Patient` entry in `rest[0].resource`
- **THEN** it contains an `operation` with `name` = `match`
- **AND** that operation's `definition` = `http://hl7.org/fhir/OperationDefinition/Patient-match`

#### Scenario: DIBBs auto-detects $match support

- **WHEN** a DIBBs-style client fetches `/metadata` and searches `rest[0].resource` for a `Patient` resource with an `operation` named `match` (or a global `rest[0].operation` named `match`)
- **THEN** it finds the `match` operation and determines that the server supports `Patient/$match`

### Requirement: Destination-agnostic conformance document

The endpoint SHALL return the same `CapabilityStatement` for any value of `{destinationId}`. It MUST NOT perform a destination lookup and MUST NOT return `404` or an `OperationOutcome` for unrecognized destination identifiers.

#### Scenario: Unknown destination still returns the CapabilityStatement

- **WHEN** an authorized client sends `GET /fhir/{destinationId}/metadata` with a `destinationId` that is not a configured destination
- **THEN** the service responds with HTTP `200` and the same `CapabilityStatement` it returns for any other `destinationId`

#### Scenario: Identical document across destinations

- **WHEN** the endpoint is called with two different `destinationId` values
- **THEN** the returned `CapabilityStatement` content is identical

### Requirement: Authorization consistent with FHIR endpoints

The `/metadata` endpoint SHALL enforce the same authorization as the other FHIR endpoints on the controller, requiring the `XFORM_SENDING_SYSTEM` or `ADMIN` role (mTLS client-certificate based). Clients probe `/metadata` using their configured client certificate.

#### Scenario: Authorized client is served

- **WHEN** a client presenting a certificate mapped to the `XFORM_SENDING_SYSTEM` or `ADMIN` role calls `GET /fhir/{destinationId}/metadata`
- **THEN** the request is authorized and the `CapabilityStatement` is returned with HTTP `200`

#### Scenario: Unauthorized client is rejected

- **WHEN** a client lacking the required role calls `GET /fhir/{destinationId}/metadata`
- **THEN** the request is rejected by the existing access-control mechanism and no `CapabilityStatement` is returned
