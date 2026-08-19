## Why

Validating real Z42 (`RSP^K11`, evaluated history + forecast) responses from live IIS test systems
(Nevada, Alaska) against a running Transformation Service for the eHealthExchange project surfaced
two FHIR R4 conformance defects in the searchset filter that post-processes every FHIR query
response (`FhirController.filter`):

1. Every `_include` / `_revinclude` hit is labelled `search.mode = "match"`, so a client cannot
   distinguish the resources it asked for from the resources joined in to support them.
2. The filter deletes resources that surviving entries still reference, so the delivered bundle
   ships mandatory (1..1) references — `ImmunizationRecommendation.patient`, `Immunization.patient` —
   that resolve to nothing in the bundle and to no endpoint this service serves.

Both are pre-existing, both are independent of the concurrent `v2tofhir` change
(`fix-z42-history-forecast-split`), and both are now highly visible on the Z42 path: after that
change the recommendation is often the *only* `match` in the bundle, so its dangling `patient` and
`authority` references are all the client has.

## What Changes

- **`_include` / `_revinclude` results are labelled `include`, not `match`.** `checkReferences`
  stamps `SearchEntryMode.MATCH` on every join target (`FhirController.java:1182`); it SHALL stamp
  `SearchEntryMode.INCLUDE`. R4 defines `include` as exactly "added to the results because of a
  join", and clients are expected to filter on `mode = 'match'` to get the hits. **BREAKING** for
  any client that relies on includes being labelled `match` — that reliance is a spec violation, and
  the only path that ever produced `INCLUDE` today is the hard-coded `_include=Resource:source:<type>`
  pseudo-parameter (`:1107`).
- **No entry in a returned searchset carries a reference to a resource absent from that searchset.**
  Resources still referenced by a surviving entry SHALL be retained with `search.mode = "include"`.
  R4 sanctions this without the client asking: "the server has the prerogative to return additional
  search results if it believes them to be relevant."
  - Concretely this restores the `Patient` (referenced by `Immunization.patient` and
    `ImmunizationRecommendation.patient`, both 1..1 required) and the schedule `Organization`
    referenced by `ImmunizationRecommendation.authority`.
  - The fix is at the filter, not at the Z42 path, because two *different* removal paths produce the
    dangling reference and both affect `/Immunization` as well as `/ImmunizationRecommendation`:
    `Patient` survives `preFilter` unmarked and is dropped by `cleanupBundleOfUnmarkedResources`
    (`:1166`); the schedule `Organization` carries `Parser.SOURCE` and is dropped earlier by
    `removeInfrastructureCreatedResources` (`:1120`).

### Non-goals

- **Forecast `Observation` resources stay filtered out of a query that does not ask for them.**
  `v2tofhir` emits one `Observation` per OBX (92 for the Nevada message); all but two duplicate
  data already carried on the `recommendation` component. Recovering the two that do not
  (`30982-3` Reason Code, `59779-9` Schedule Used) is being handled in `v2tofhir` by mapping them
  to `recommendation.forecastReason` and `ImmunizationRecommendation.authority`. Explicitly
  considered and rejected — no change here.

  A caller who passes `_revinclude=Observation` still gets them, as they always could: the filter
  walks the reverse direction only on an explicit parameter, so the default query returns none.
  That is the R4-correct division and is not a goal of this change either way.
- `.gitignore` for `ehex-testing/` and the `v2tofhir` `2.4.0` → `2.5.0` version bump are handled
  manually outside this change.

## Capabilities

### New Capabilities

- `fhir-searchset-filtering`: How the service post-processes a converted response bundle into a
  FHIR R4 searchset — which entries are retained, and what `Bundle.entry.search.mode` each entry
  carries. Covers `match` / `include` / `outcome` classification, `_include` / `_revinclude`
  resolution against the search names `v2tofhir` registers on `Reference.userData`, the
  `_include=Resource:source:<type>` pseudo-parameter for infrastructure-created resources, and
  bundle-internal reference integrity. Applies to every FHIR query endpoint
  (`/Immunization`, `/ImmunizationRecommendation`, `/Patient`, `Patient/$match`), so it does not
  belong under `fhir-immunization-query`.

### Modified Capabilities

None. `fhir-immunization-query` covers request-side `subject`→`patient` aliasing and is untouched.

## Impact

- **Code**: `src/main/java/gov/cdc/izgateway/xform/endpoints/fhir/FhirController.java` only —
  `checkReferences`, `preFilter` / `cleanupBundleOfUnmarkedResources` /
  `removeInfrastructureCreatedResources`. No new class.
- **Inbound path**: FHIR REST only. The SOAP/HL7 v2 inbound path (`IISHubService`, `IISService`) is
  untouched.
- **Outbound path**: none. The outbound QBP_Q11 / Z44 / Z34 query and the `izghub` / `iis` producer
  contracts are unchanged — this change only reshapes the response bundle returned to the FHIR
  caller.
- **Config model**: unchanged. No `Organization` / `Pipeline` / `Solution` / `Operation` /
  `Precondition` change, so no `dependency` on either repository backend and no
  `SPRING_DATABASE=migrate` implication. Existing organization transformation configurations are
  unaffected.
- **Downstream Hub/IIS consumers**: unaffected — they see no FHIR.
- **FHIR clients**: bundles gain up to two entries per response (`Patient`, schedule `Organization`),
  both labelled `include`; `_include` / `_revinclude` entries change label from `match` to
  `include`. `docs/CONFIGURATION_REFERENCE.md` needs no change (no new property).
- **Tests**: no existing test in `src/test` references `_include`, `_revinclude`, or has a Z42
  fixture, and no test asserts on `Immunization` / `ImmunizationRecommendation` bundle content — so
  this behaviour is entirely uncovered today. `FhirControllerTests` should stay green; new coverage
  is needed.
- **Dependencies**: none added.
