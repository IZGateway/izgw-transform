## Why

In the eHealth Exchange immunization pilot, DIBBs Query Connector calls xform's `POST /fhir/{destinationId}/Patient/$match`. When the IIS finds more candidates than its record limit, it returns an HL7 v2 Z33 response with a "too much data found" query status (QAK-2 = `TM`) and zero patient records. Today xform converts that into a `searchset` Bundle containing only informational OperationOutcome entries, so DIBBs (v1.2.0) — which only recognizes a *top-level* `OperationOutcome` whose `issue.details.text` contains "did not find a certain match" — shows "No Records Found", indistinguishable from a true no-match. The "too many matches, refine your search" signal is lost. Returning a top-level OperationOutcome for this case lets stock DIBBs show its built-in "No Certain Match Found" message with no DIBBs code change, and is the spec-sanctioned FHIR shape for an unsuccessful `$match` (particularly with `onlyCertainMatches: true`, which DIBBs sends).

## What Changes

- `FhirController.iisPatientMatch()` gains a branch: when the converted query response contains **zero Patient entries** and an OperationOutcome issue carrying the "too much data found" query status (`details.coding` system `http://terminology.hl7.org/CodeSystem/v2-0208`, code `TM`), respond with a top-level `OperationOutcome` instead of the searchset Bundle:
  - `issue[0].severity` = `warning`, `issue[0].code` = `multiple-matches`
  - `issue[0].details.text` = "The matching operation found one or more possible matches, but did not find a certain match." (contains the exact DIBBs trigger phrase "did not find a certain match")
  - `issue[0].details.coding` retains the v2-0208 `TM` coding for provenance / non-DIBBs consumers
  - HTTP status **422 Unprocessable Entity** (decided; aligns with the FHIR Operations framework's "unsuccessful → OperationOutcome + error status"; DIBBs evaluates the body before the status, so it is unaffected)
- All other `$match` outcomes are unchanged: ≤ count candidates and single/certain match still return the `searchset` Bundle of scored Patient resources with HTTP 200; a true no-match (`NF`) still returns the empty Bundle.
- GET/POST search paths (`/Patient`, `/Immunization`, `/ImmunizationRecommendation`, `/_search`) are untouched — this applies to the `$match` operation only.
- Integration tests (Postman collection in `testing/scripts/`) cover both response shapes (top-level OperationOutcome and searchset Bundle).

## Capabilities

### New Capabilities
- `fhir-patient-match`: Behavior of the `POST /fhir/{destinationId}/Patient/$match` operation — response shapes per query outcome, including the top-level OperationOutcome for the "too many matches / no certain match" case and the searchset Bundle of scored candidates otherwise.

### Modified Capabilities

(none — `fhir-immunization-query` covers the `subject` search-parameter alias only; no existing requirement changes)

## Impact

- **Affected code**: `gov.cdc.izgateway.xform.endpoints.fhir.FhirController#iisPatientMatch()` only (plus possibly a small helper for detecting the TM outcome). `IDIMatch`, `processQuery`, and the Camel/SOAP pipeline are unchanged.
- **Inbound/outbound paths**: FHIR inbound path only. The SOAP/HL7v2 inbound paths (`/IISHubService`, `/IISService`) and outbound hub/IIS forwarding are untouched.
- **Config model**: No changes to Organization/Pipeline/Solution/Operation/Precondition; no repository (file/DynamoDB) or migration impact.
- **Downstream consumers / backward compatibility**: Consumers of `$match` that previously received a Bundle with zero patients for the TM case will now receive a 422 + OperationOutcome. This is a deliberate, spec-valid behavior change scoped to an outcome that carried no usable patient data; the acknowledged consumer (DIBBs Query Connector) handles it correctly. All Bundle-returning outcomes are unchanged.
- **Dependencies**: Relies on v2tofhir's `QAKParser` mapping QAK-2 into an OperationOutcome issue with the v2-0208 coding (verified in current v2tofhir). The former prerequisite IGDD-3164 (Accept-header/Content-Type handling on `$match` POST) is already fixed on this branch (`94699f9cb`). Related: IGDD-3165 (v2tofhir "too many" → warning severity) is complementary but not required; IGDD-3172 (CapabilityStatement) is needed for stock DIBBs to select the `$match` path but is a separate change.
