## ADDED Requirements

### Requirement: Accept `subject` as a lenient alias for `patient`

The service SHALL accept a `subject` search parameter as an alias for `patient` on the FHIR
query endpoints (`GET /fhir/{destination}/Immunization`, `/ImmunizationRecommendation`,
`/Patient`, and the corresponding `POST .../_search` form variant). When a `subject`
parameter whose value resolves to a `Patient` reference is present and no `patient` parameter
is supplied, the service SHALL treat it exactly as if the same value had been supplied as
`patient`, producing an identical HL7 v2 QBP_Q11 query. The `v2tofhir` mapping SHALL NOT be
modified; the alias is applied by rewriting the request input before mapping.

This is a non-standard convenience extension — `subject` is not a defined Immunization search
parameter in FHIR R4 or US Core. It SHALL be documented as such, and senders SHOULD migrate
to `patient=`. Existing requests using `patient`, `patient.identifier`, or the `patient.*`
demographics SHALL be unaffected (backward compatible). The outbound QBP_Q11 and the SOAP/FHIR
message contracts to downstream Hub/IIS consumers SHALL be unchanged.

#### Scenario: subject with a typed Patient reference is aliased to patient
- **GIVEN** a request to `GET /fhir/{destination}/Immunization`
- **WHEN** the request supplies `subject=Patient/<id>` and no `patient` parameter
- **THEN** the service SHALL produce the same QBP_Q11 query (QPD-3 patient identifier) it
  would produce for `patient=Patient/<id>`, and the query SHALL pass identifier validation

#### Scenario: subject with a bare id is aliased to patient
- **GIVEN** a request to a FHIR query endpoint
- **WHEN** the request supplies `subject=<id>` with no resource-type prefix and no `patient`
  parameter
- **THEN** the service SHALL treat the value as a `patient` reference and build the
  corresponding QPD-3 patient identifier

#### Scenario: alias applies to the POST _search form variant
- **GIVEN** a request to `POST /fhir/{destination}/Immunization/_search` with an
  `application/x-www-form-urlencoded` body
- **WHEN** the body contains `subject=Patient/<id>` and no `patient`
- **THEN** the service SHALL apply the same `subject`→`patient` aliasing as for the GET query

#### Scenario: existing patient queries are unaffected
- **GIVEN** a request that already uses `patient`, `patient.identifier`, or `patient.*`
  demographics and no `subject`
- **WHEN** the request is processed
- **THEN** the resulting QBP_Q11 query SHALL be identical to the behavior before this change

### Requirement: Patient-only guard on the `subject` alias

The service SHALL alias a `subject` value to `patient` only when the value resolves to a
`Patient` reference or is a bare id with no resource type. A `subject` value that is an
explicitly-typed non-Patient reference (for example `Group/<id>`) SHALL NOT be aliased and
SHALL be dropped, so the request falls through to the existing identifier/name+birthDate
validation rather than producing a reference-decode error.

#### Scenario: non-Patient typed reference is not aliased
- **GIVEN** a request to a FHIR query endpoint
- **WHEN** the request supplies `subject=Group/<id>` and no other patient-identifying
  parameters
- **THEN** the `subject` value SHALL NOT be mapped to a patient identifier
- **AND** the service SHALL return `400 Bad Request` with the existing validation message
  ("A query must contain either a patient.identifier or the patient name and birthDate")
  rather than a decode error

#### Scenario: a non-Patient subject does not block a valid patient parameter
- **GIVEN** a request that supplies both a valid `patient=Patient/<id>` and a
  `subject=Group/<id>`
- **WHEN** the request is processed
- **THEN** the `patient` parameter SHALL be used and the `subject` value SHALL be ignored

### Requirement: An existing patient identifier takes precedence over `subject`

When a request already identifies the patient via `patient` (a reference) **or**
`patient.identifier`, the service SHALL use that and SHALL ignore `subject`, so no
duplicate or conflicting patient identifier is added to the query (QPD-3).

#### Scenario: both patient and subject supplied
- **GIVEN** a request that supplies `patient=Patient/<idA>` and `subject=Patient/<idB>`
- **WHEN** the request is processed
- **THEN** the resulting QBP_Q11 query SHALL be built from `<idA>` only, identical to a
  request supplying `patient=Patient/<idA>` alone

#### Scenario: both patient.identifier and subject supplied
- **GIVEN** a request that supplies `patient.identifier=<system|value>` and a
  `subject=Patient/<id>` reference
- **WHEN** the request is processed
- **THEN** `subject` SHALL NOT be aliased to `patient`, and the query SHALL be built from
  `patient.identifier` only — no second QPD-3 identifier is added
