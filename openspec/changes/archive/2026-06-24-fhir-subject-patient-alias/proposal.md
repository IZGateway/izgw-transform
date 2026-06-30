## Why

A downstream sender (Skylight, via the eHealth Exchange proxy) issues FHIR Immunization
queries using `subject=Patient/<id>` instead of `patient=<id>`. `subject` is not a defined
search parameter for the Immunization resource in FHIR R4 or US Core, so the Xform Service
silently ignores it and the query fails with `400 Bad Request` — *"A query must contain
either a patient.identifier or the patient name and birthDate."* Following Postel's law, we
will accept `subject` as a lenient alias for `patient` so these queries succeed, delivering a
working integration sooner while the sender migrates to `patient=`.

## What Changes

- The FHIR query endpoint (`GET /fhir/{destination}/Immunization`,
  `/ImmunizationRecommendation`, `/Patient`, and the corresponding `POST .../_search`)
  accepts a `subject` search parameter as an alias for `patient`.
- When a `subject` parameter is present, the Xform Service rewrites it to `patient` before
  the request is mapped to an HL7 v2 QBP_Q11 query, so the spec-compliant `v2tofhir` mapping
  is unchanged and unaware of the alias.
- A **Patient-only guard** limits the alias: only references that resolve to a `Patient`
  (or a bare id) are aliased. An explicitly-typed non-Patient reference (e.g. `Group/...`)
  is dropped, so the request falls through to the existing "no identifier" validation rather
  than producing a decode error.
- If both `patient` and `subject` are supplied, `patient` takes precedence and `subject` is
  ignored.
- This is a **non-standard convenience extension**, not FHIR conformance; it will be
  documented as such, and senders should migrate to `patient=`.
- Not a breaking change: requests that already use `patient`/`patient.identifier`/
  `patient.*` are unaffected.

## Capabilities

### New Capabilities
- `fhir-immunization-query`: Behavior of the FHIR-to-QBP query endpoint's search-parameter
  handling — specifically accepting `subject` as a lenient, Patient-only alias for `patient`,
  with precedence rules and the non-Patient drop behavior.

### Modified Capabilities
<!-- None. No existing capability spec defines this behavior (only `api-documentation` exists),
     and no existing requirements change. -->

## Impact

- **Inbound path:** FHIR REST only (`xform/endpoints/fhir`). SOAP/HL7v2 inbound paths are
  unaffected.
- **Outbound paths:** None changed. The outbound message to `izghub`/`iis` is a normal
  QBP_Q11 patient query identical to what `patient=` already produces — no backward-compat
  risk for Hub/IIS consumers.
- **Code:** `FhirController.processQuery` (the single chokepoint for GET, `POST .../_search`,
  and the `$match` flows) plus the existing `RequestWithModifiableParameters` wrapper. Uses
  HAPI's `ReferenceParam` to classify the reference type.
- **Dependencies:** No change to `v2tofhir` (`QBPUtils`/`IzQuery`) — it remains
  spec-compliant; the Xform Service adjusts its own input as a client.
- **Config model & storage:** No change. No `Organization`/`Pipeline`/`Solution`/`Operation`/
  `Precondition` changes; no file or DynamoDB repository impact and no `SPRING_DATABASE=migrate`
  implications.
- **Docs:** Update the FHIR endpoint reference (`docs/fhir/`) to note `subject` is accepted as
  a non-standard alias for `patient`.
- **Tests:** New JUnit coverage for the alias, the Patient-only guard, and `patient` precedence.
