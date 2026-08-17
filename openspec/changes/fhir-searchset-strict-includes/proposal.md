## Why

A FHIR query should return the resource type the caller asked for and nothing else. Anything
beyond that is the caller's decision, expressed with `_include` and `_revinclude`. The
`fhir-searchset-filtering` capability, as it stands on the `IGDD-3285` branch, breaks that rule: a
plain `GET /fhir/{destination}/Immunization` also returns a `Patient`, and a plain
`GET /fhir/{destination}/ImmunizationRecommendation` also returns a `Patient`, an `Organization`,
and every `Immunization` in the Z42 evaluated history — none of them requested. The branch reached
that behavior while closing a real defect (mandatory 1..1 references such as `Immunization.patient`
resolved to nothing in the delivered bundle), but the cure changed the contract the caller sees:
they can no longer predict the shape of a response from the query they sent.

This change restores the strict contract before the branch merges. Reference resolution goes back
to what `develop` does today — an unresolved reference keeps its literal value and the caller
resolves it, or does not, on their own terms.

## What Changes

- **BREAKING** A FHIR query response contains only entries of the requested resource type
  (`search.mode = "match"`) and `OperationOutcome` entries (`search.mode = "outcome"`). Every other
  resource in the converted bundle is removed unless the caller asked for it.
- **BREAKING** Referenced resources are no longer retained just because a returned entry points at
  them. `Patient` no longer accompanies an `/Immunization` query, and `Patient` and the schedule
  `Organization` no longer accompany an `/ImmunizationRecommendation` query. Callers that want them
  send `_include=Immunization:patient`, `_include=ImmunizationRecommendation:patient`,
  `_include=ImmunizationRecommendation:authority`, or a wildcard such as `_include=*:*`.
- **BREAKING** The Z42 evaluated-history `Immunization` resources are no longer returned on an
  `/ImmunizationRecommendation` query. A caller who wants the evaluated history and its OBX-derived
  evaluation data asks for it — `_include=ImmunizationRecommendation:patient` together with
  `_revinclude=Immunization:patient`, or `_include=*:*&_revinclude=*:*`.
- A reference whose target is not in the returned bundle keeps its `reference` element unchanged.
  The service no longer strips the element down to `identifier` and `display`. This matches
  `develop` and matches how a FHIR server ordinarily answers a search: the literal reference is the
  caller's to resolve.
- Unchanged from the branch, and deliberately kept: an `_include` or `_revinclude` hit is labelled
  `search.mode = "include"`, not `match`, so a caller can still isolate the hits their query asked
  for by selecting `mode = "match"`.
- Unchanged from the branch: conversion-created resources (those carrying `Parser.SOURCE`) stay
  opt-in through `_include=Resource:source:<type>` and `_include=Resource:source:*`. Because the
  branch defers all removal to a single cleanup pass, a bare `_revinclude=Provenance` now works
  through the ordinary include path and needs no special case.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `fhir-searchset-filtering`: Removes the requirement "A returned searchset contains no dangling
  references" and the requirement "A recommendation query returns the evaluated history it was
  sent with". Rewrites the reference-handling behavior so an unresolved reference is left intact
  rather than reduced to a logical reference. The requirements covering searchset typing, `match`
  labelling, `outcome` labelling, `include` labelling, the conversion-created white-list, and the
  removal of unclassified entries are unchanged in intent, but the removal requirement now governs
  strictly more entries.

## Impact

- **Inbound paths**: FHIR REST only. The SOAP/HL7 v2 inbound path (`IISHubService`, `IISService`)
  is untouched.
- **Outbound paths**: none. The query sent downstream to `izghub` or `iis` is unchanged, as is the
  HL7 v2 request built for it. No downstream Hub or IIS consumer sees any difference.
- **Config model**: unchanged. No `Organization`, `Pipeline`, `Solution`, `Operation`, or
  `Precondition` change, and searchset assembly consults no organization configuration. Existing
  organization transformation configurations are unaffected.
- **Repository backends**: unchanged. Nothing is persisted by this change, so neither the file nor
  the DynamoDB backend is touched and `SPRING_DATABASE=migrate` has no migration implication. No
  entry in `docs/CONFIGURATION_REFERENCE.md` or `docs/APPLICATION_CONFIGURATION_STORAGE.md`
  changes; no runtime property is added.
- **Code**: `FhirController.java` — remove `retainReferencedResources` and its call, remove
  `clearUnresolvableReferences` with its `toRelativeReference` and `forEachReference` helpers, and
  remove the Z42 `Immunization` branch from `preFilter`.
- **Tests**: `FhirControllerTests.java` — the assertions added on this branch for auto-retained
  `Patient`/`Organization`, for the Z42 evaluated history, and for reference stripping are replaced
  by assertions that those resources are absent by default and present when asked for, and that an
  unresolved reference keeps its literal value.
- **Backward compatibility**: this is a breaking change for a FHIR caller that relies on receiving
  unrequested resources. The eHealth Exchange pilot is the known consumer; it must add the
  `_include` / `_revinclude` parameters for anything beyond the requested type. Callers already on
  `develop` behavior see no change other than `include` in place of `match` on join hits.
- **Docs**: `docs/fhir/fhir-api.md` and `docs/fhir/rsp-to-fhir.md` describe the auto-retain
  behavior and the reference stripping, and both need rewriting around the strict contract. The
  CapabilityStatement declares no `searchInclude` values, so it needs no change.
