## Context

The FHIR query endpoints in `xform/endpoints/fhir/FhirController` translate FHIR search
parameters into an HL7 v2 QBP_Q11 query, which is forwarded to the IIS Hub. Parameter
mapping is delegated to the spec-compliant `v2tofhir` library (`QBPUtils` → `IzQuery`):
`IzQuery` understands `patient` (a reference, decoded via `FhirIdCodec` to a `system|value`
identifier placed in QPD-3), `patient.identifier`, and the `patient.*` demographics. It does
**not** understand `subject`, because `subject` is not a search parameter on the Immunization
resource in FHIR R4 or US Core.

A downstream sender (Skylight, via the eHealth Exchange proxy) sends `subject=Patient/<id>`,
where `<id>` is the URL-base64 encoding of `system|value` (e.g. something like `TEST|0000001`). Because
`subject` is ignored, the query reaches `IzQuery.update()` with no patient identifier and
fails with `400` — *"A query must contain either a patient.identifier or the patient name and
birthDate."*

All FHIR query traffic — `GET /fhir/{destination}/{Immunization|ImmunizationRecommendation|
Patient}`, the `POST .../_search` form variant, and the internal `$match` flows — funnels
through `FhirController.processQuery(HttpServletRequest, String)`. That single method is the
natural place to adjust input. This change does not touch the SOAP/HL7v2 inbound path, the
Camel pipeline, the config model, or either repository backend, so most of the standard
architectural concerns (FIPS/BCFKS crypto, AspectJ advice, file vs DynamoDB) are not engaged.

## Goals / Non-Goals

**Goals:**
- Accept `subject` as a lenient alias for `patient` on the FHIR query endpoints so
  `subject=Patient/<id>` requests succeed exactly as `patient=Patient/<id>` would.
- Keep `v2tofhir` spec-compliant and unmodified — the Xform Service adjusts its own input.
- Cover GET, `POST .../_search`, and the `$match` flows with one change.
- Fail safely on unexpected/unsupported `subject` shapes rather than erroring obscurely.

**Non-Goals:**
- No change to `v2tofhir` (`QBPUtils`/`IzQuery`/`FhirIdCodec`).
- No new outbound behavior — the QBP_Q11 produced is identical to the `patient=` case.
- Not claiming FHIR conformance for `subject` on Immunization; this is a vendor courtesy.
- No config-model, repository, SOAP, or Camel-pipeline changes.

## Decisions

### D1 — Normalize in the Xform Service, not in v2tofhir
Rewrite `subject` → `patient` in `FhirController` before mapping. **Alternative considered:**
add a `subject` case to `IzQuery`'s switch in `v2tofhir`. Rejected because `v2tofhir` is a
shared, spec-compliant library used by other consumers; baking a non-standard alias into it
would bend the library's conformance for everyone and require a library release + `izgw-bom`
bump to ship. Treating Xform as a client that fixes up its own input is the smaller, faster,
better-scoped change. (This reverses an earlier consideration of doing it in `v2tofhir`.)

### D2 — Normalize at the `processQuery` chokepoint
Do the rewrite as the first statement of `FhirController.processQuery`. **Alternative:**
do it in each entry method (`iisQuery`, `iisSearchPost`) — rejected as duplicative and
easy to miss for the `$match` flows, which also call `processQuery`. One chokepoint covers
every path.

### D3 — Reuse `RequestWithModifiableParameters`
The repo already has `RequestWithModifiableParameters` (an `HttpServletRequestWrapper` with a
mutable parameter map and `addParameter`/`removeParameters`), and `setQueryParameters` already
branches on `req instanceof RequestWithModifiableParameters`. Wrap the request only if it is
not already wrapped (the `$match` flows pass one in), copy `subject` values to `patient`, and
remove `subject`. **Alternative:** a servlet `Filter` — rejected as heavier and farther from
the existing pattern.

### D4 — Patient-only guard via HAPI `ReferenceParam`
Only alias a `subject` value whose reference resolves to a `Patient` (or a bare id with no
resource type); drop explicitly-typed non-Patient references (e.g. `Group/...`). Classify the
reference with `ca.uhn.fhir.rest.param.ReferenceParam` — the same parser `IzQuery` uses for
`patient` — so "what counts as a Patient reference" is judged identically on both sides and
handles `Patient/123`, bare ids/base64 tokens, and full-URL references. **Alternative:**
a blind rename with no guard (simplest, matches the literal "just replace it" framing).
Rejected because the upstream system is outside our control and may send other `subject`
shapes; a blind rename of `Group/...` would feed garbage to `FhirIdCodec.decode` and surface
an ugly decode error instead of the existing clean "no identifier" 400. The guard is ~6 lines
and converts a future surprise into a well-defined response. **Alternative:** hand-rolled
string parsing of the resource type — rejected as fragile for URL-form references.

### D5 — `patient` takes precedence
If both `patient` and `subject` are present, leave `patient` untouched and ignore `subject`.
This keeps the standard parameter authoritative and avoids duplicate/conflicting QPD-3 entries.

### Request flow (with normalization)

```
FHIR client
   │  GET /fhir/{dest}/Immunization?subject=Patient/<b64>
   │  (or POST .../_search form body)
   ▼
FhirController.iisQuery / iisSearchPost / $match flows
   ▼
FhirController.processQuery(req, dest)
   │  req = normalizeSubjectToPatient(req)      ← THIS CHANGE
   │     • subject present? wrap in RequestWithModifiableParameters
   │     • for each subject value that isPatientReference(): addParameter("patient", v)
   │     • removeParameters("subject")
   ▼
setQueryParameters → QBPUtils.addParamsToQPD → IzQuery   (v2tofhir, unchanged)
   │     • patient → FhirIdCodec.decode → QPD-3 (system|value, type MR), hasId=true
   ▼
QBP_Q11 → HubController.submitSoapRequest → IIS Hub
   ▼
RSP_K11 → convertResponseToFHIR → Bundle (response)
```

There is no loopback or response-direction interaction here — normalization happens once on
the inbound request before the QBP is built; the response path is unchanged.

## Risks / Trade-offs

- **Non-standard behavior diverges from strict FHIR.** → Documented explicitly as a vendor
  convenience in `docs/fhir/`; senders are told to migrate to `patient=`. Easily removed later.
- **A future `subject` shape we didn't anticipate.** → The Patient-only guard drops anything
  that isn't a Patient reference, so the worst case is the existing clean validation 400, not a
  500 or a malformed query.
- **`ReferenceParam` parsing of an odd value could behave unexpectedly.** → Bare/blank values
  are short-circuited; `ReferenceParam` is the same parser already trusted downstream for
  `patient`, so behavior is consistent with existing handling.
- **Checkstyle.** → `subject`/`patient` are pulled into constants (reuse existing
  `PATIENT = "Patient"` for the type check; `Immunization.SP_PATIENT` for the param name) to
  stay under `MultipleStringLiterals`; two short private methods keep complexity/length low.

## Migration Plan

- Pure additive runtime behavior; no schema, config, or data migration. No `SPRING_DATABASE`
  implications. Deploy is a normal image roll-out.
- **Rollback:** revert the commit; `subject` returns to being ignored (pre-change behavior).
  No persisted state to undo.
- **Coordination:** out of band, ask Skylight to migrate to `patient=`; this alias is the
  bridge, not the destination.

## Open Questions

- **Always-on vs. configurable?** v0 ships always-on (simplest, matches "deliver sooner").
  If a future operator wants strict FHIR behavior, the alias could be gated behind a config
  flag — deferred unless requested.
