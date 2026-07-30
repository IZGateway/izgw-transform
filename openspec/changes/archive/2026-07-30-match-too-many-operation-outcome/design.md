## Context

`POST /fhir/{destinationId}/Patient/$match` (`FhirController#iisPatientMatch`) converts the incoming Patient/Parameters into an HL7 v2 QBP query via `processQuery()`, which submits through `HubController.submitSoapRequest()` and converts the RSP response back to a FHIR Bundle with v2tofhir's `MessageParser`. `IDIMatch.score()` then scores the Patient entries and the searchset Bundle is returned with HTTP 200.

When the IIS finds more candidates than its record limit it returns a Z33 response with QAK-2 = `TM` ("too much data found") and no PID groups. v2tofhir's `QAKParser` converts QAK into an OperationOutcome entry whose `issue.details.coding` carries system `http://terminology.hl7.org/CodeSystem/v2-0208` and the raw QAK-2 code (`TM`), with informational severity. The resulting `$match` response is a Bundle with OperationOutcome `outcome` entries and zero Patients.

DIBBs Query Connector (v1.2.0, `query-execution/service.ts`) only recognizes the no-certain-match condition when the **top-level** response body is an `OperationOutcome` with "did not find a certain match" in some `issue[].details.text`. It never descends into Bundle entries, so today it renders "No Records Found" — losing the "refine your search" signal. Its body check runs before its HTTP-status check, so a non-200 status is safe.

This change is FHIR-inbound only. The transformation pipeline (Camel route → `DataXformService` → `PipelineRunnerService`) still runs on the underlying SOAP exchange exactly as before — the branch added here operates purely on the *converted FHIR representation* inside `FhirController`, after the hub round-trip returns. No pipeline, config-model, or repository behavior changes, so both file and DynamoDB backends are unaffected, and no crypto/SSL/AspectJ surface is touched.

## Goals / Non-Goals

**Goals:**
- On the `$match` path, detect the "too many / no certain match" outcome and return a top-level `OperationOutcome` (HTTP 422) whose `issue.details.text` contains the exact DIBBs trigger phrase "did not find a certain match".
- Preserve the v2-0208 `TM` coding in the response for provenance and non-DIBBs consumers.
- Leave every other `$match` outcome (candidates returned, certain match, true no-match `NF`, errors) byte-for-byte unchanged.
- Cover both response shapes in the Postman/Newman integration suite.

**Non-Goals:**
- No change to GET/POST search endpoints (`/Patient`, `/Immunization`, `/ImmunizationRecommendation`, `/_search`) or read endpoints.
- No change to v2tofhir (IGDD-3165 is complementary and independent) and no CapabilityStatement work (IGDD-3172).
- No gating on `onlyCertainMatches` — the TM outcome carries zero candidates regardless, so the OperationOutcome is the correct response either way.
- No transformation-pipeline, config-model, or persistence changes.

## Decisions

### 1. Detect via `TM` coding + zero Patient entries (not the Z33 profile)

The branch triggers when the converted Bundle contains **no Patient resources** AND some OperationOutcome entry has an issue with a `details.coding` whose system is `http://terminology.hl7.org/CodeSystem/v2-0208` and code is `TM` (case-insensitive).

- *Why not sniff `meta.profile` for `Z33`?* The profile string is IIS-population-dependent metadata; the QAK-derived coding is the semantic payload and is guaranteed by `QAKParser`'s field mapping (`@ComesFrom(field = 2, table = "0208")`).
- *Why also require zero Patients?* Defensive: if an IIS ever returned candidates alongside a TM status, dropping them would be worse than showing them. The zero-Patient guard makes the reshape strictly lossless.
- *Why not key on issue severity?* `QAKParser` currently emits `information` for TM, but IGDD-3165 may change it to `warning`. Keying on the coding keeps this change independent of v2tofhir version.

### 2. Branch location: `iisPatientMatch`, immediately after `processQuery()`

The check runs on the Bundle returned by `processQuery(...)`, before `IDIMatch.score()` (scoring is a no-op with zero Patients, but branching first keeps the two response paths visibly disjoint). All other callers of `processQuery` (searches, reads) are untouched. The method already returns `ResponseEntity<Resource>`, so returning an `OperationOutcome` needs no signature change.

Detection and response construction go in a small static helper (package-private for unit testing), keeping `iisPatientMatch` within Checkstyle complexity/length limits. Repeated string literals (`v2-0208` system URL, response text) become constants per `MultipleStringLiterals`.

### 3. Response shape

```json
{
  "resourceType": "OperationOutcome",
  "issue": [{
    "severity": "warning",
    "code": "multiple-matches",
    "details": {
      "coding": [{ "system": "http://terminology.hl7.org/CodeSystem/v2-0208", "code": "TM", "display": "..." }],
      "text": "The matching operation found one or more possible matches, but did not find a certain match."
    }
  }]
}
```

- `IssueType.MULTIPLEMATCHES` exists in the HAPI R4 model — this is the semantically precise R4 issue-type code.
- The `details.coding` is **carried over from the detected source issue** (rather than constructed fresh) so any display text or additional codings the IIS/converter provided survive.
- `details.text` is a complete sentence containing the DIBBs substring `did not find a certain match`. The substring match means the wording can be extended but the phrase itself must never be reworded — the spec pins it.

### 4. HTTP 422, negotiated content type

Status **422 Unprocessable Entity** (decided with stakeholder): the FHIR Operations framework prescribes "unsuccessful → OperationOutcome + error status", and `$match` with `onlyCertainMatches: true` and no certain match is unsuccessful. DIBBs reads the body before the status, so it is unaffected. Headers come from `ContentUtils.getHeaders(req)` exactly as the existing `$match` return does, preserving the Accept-header negotiation fixed in `94699f9cb`.

Alternative considered: HTTP 200 (gentler on unknown consumers that alarm on non-2xx). Rejected as the less spec-aligned option; the outcome carried no usable data before, so no consumer loses information.

### 5. Error handling

- Detection is null-safe over `bundle`, entries, issues, and codings; when in doubt it does **not** trigger, falling through to today's Bundle path (fail-open to current behavior).
- Existing exception handlers (`FaultException`, `HL7Exception`, etc.) are untouched — IIS-level errors (QAK `AE`/`AR`) still surface through the current paths.

## Sequence

```mermaid
sequenceDiagram
    participant D as DIBBs Query Connector
    participant F as FhirController.iisPatientMatch
    participant H as HubController (SOAP + xform pipeline)
    participant I as IIS

    D->>F: POST /fhir/{dest}/Patient/$match (Parameters, onlyCertainMatches=true)
    F->>F: build QBP Z34 from Patient demographics
    F->>H: submitSoapRequest (request pipeline runs)
    H->>I: QBP^Q11
    I-->>H: RSP (Z32/Z31 or Z33 QAK-2=TM)
    H-->>F: SubmitSingleMessageResponse (response pipeline runs, pipes reversed)
    F->>F: MessageParser.convert → Bundle; filter; adjustIdentifiers
    alt zero Patients AND v2-0208 TM coding present
        F-->>D: 422 OperationOutcome (warning / multiple-matches / "did not find a certain match")
        D->>D: shows "No Certain Match Found"
    else candidates / certain match / NF
        F->>F: IDIMatch.score(bundle)
        F-->>D: 200 searchset Bundle of scored Patients
        D->>D: pick-list / "No Records Found"
    end
```

The `x-loopback` short-circuit applies to the SOAP endpoints, not this FHIR path; `$match` always performs the hub round-trip. Response-direction pipe reversal happens inside the hub exchange and is unaffected.

## Risks / Trade-offs

- [An IIS signals "too many" some other way (e.g. free-text ERR, different QAK code)] → Out of scope; detection keys on the CDC IG-standard QAK-2 `TM`. Such an IIS falls through to today's behavior — no regression.
- [Non-DIBBs `$match` consumers receive 422 + OperationOutcome where they previously got a 200 Bundle] → Deliberate, documented behavior change scoped to an outcome that contained zero patient data; spec-valid per the FHIR Operations framework. Release notes call it out.
- [DIBBs rewords its trigger phrase in a future version] → The phrase is pinned by spec scenario and integration test; a DIBBs upgrade in the pilot should re-run the verification. Low likelihood; the same phrase is what HAPI FHIR emits.
- [IGDD-3165 lands and changes QAK issue severity/shape in v2tofhir] → Detection uses the details coding, not severity; unit test fabricates the Bundle directly so it can't silently drift with the converter.
- [HEAD /Patient/$match also maps to this handler] → HEAD responses carry status but no body; a 422 on HEAD for this outcome is consistent and harmless.

## Migration Plan

No config, schema, or repository changes; no new dependencies. Deploy as a normal service release (feature branch → `develop` → dev ECS deployment → Newman suite). Rollback is a straight revert/redeploy — no data or config to unwind.

## Open Questions

None. HTTP status (422) was decided with the stakeholder; the prerequisite Accept-header fix (`94699f9cb`, IGDD-3164) is already on this branch.
