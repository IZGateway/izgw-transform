## 1. Test harness and fixtures (prerequisite)

Per `design.md` — Risks, this behaviour has zero coverage today, so the fixtures land before the
code edits and are expected to fail until section 3 and 4 are done.

Tests go in the existing `src/test/java/gov/cdc/izgateway/xform/endpoints/fhir/FhirControllerTests.java`,
which is plain JUnit 5 + Mockito and constructs `FhirController` directly through the
`controller(hubReturning(...))` helper. **Deliberate deviation from the project default:** these tests
do NOT get `@SpringBootTest`. The searchset filter needs no Spring context, only two of the repo's
test classes carry the annotation, and adding a context here would slow the suite for no coverage.
All tests still run under Maven surefire with its existing env/keystore setup.

- [x] 1.1 Extend the `fhirRequest(uri, accept)` helper (`FhirControllerTests.java:470`) to accept
      query parameters, stubbing `getParameterValues("_include")` and `getParameterValues("_revinclude")`
      alongside the existing `getParameterMap()`. Keep the two-argument form working for existing
      callers.
- [x] 1.2 Add an `RSP_Z42_MESSAGE` fixture: an `RSP^K11` with `QAK`/`QPD` of
      `Z42^Request Evaluated History and Forecast^CDCPHINVS`, `MSH-21` carrying the Z42 profile, one
      `PID`, at least two administered `ORC`/`RXA` groups (`RXA-5` a real CVX), at least one forecast
      group (`RXA-5 == 998`), and forecast `OBX` segments including `59779-9` Immunization Schedule
      Used (`VXC16^ACIP^CDCPHINVS`) so `ImmunizationRecommendation.authority` is populated.
- [x] 1.3 Add an `RSP_Z32_MESSAGE` fixture: an `RSP^K11` Z32 response with one `PID` and at least two
      administered `ORC`/`RXA` groups, so the `/Immunization` path is covered independently of Z42.
- [x] 1.4 Add a reusable assertion helper `assertNoDanglingReferences(Bundle)` that walks every
      `Reference` on every entry and asserts each populated `Reference.reference` resolves to an entry
      in the same bundle. This is the check that would have caught the withdrawn
      `supportingPatientInformation` link; it is reused by every test below.
- [x] 1.5 Confirm which `v2tofhir` version the suite compiles against (`pom.xml:110-111`). The
      history/forecast split assertions in 3.3 and 4.4 require `2.5.0`; if the tree is still on
      `2.4.0`, write them but mark them `@Disabled` with a reference to this change, and note it in
      the PR so the version bump un-disables them rather than silently skipping.

## 2. Verify current (broken) behaviour is captured

- [x] 2.1 Run `mvn test -Dtest=FhirControllerTests` and record which of the new tests fail. Confirm
      the failures are exactly the two defects — `include` labelled `match`, and a dangling
      `patient` / `authority` — and not a fixture error. A fixture that produces an empty or
      unconverted bundle proves nothing.

## 3. Fix 1 — label `_include` / `_revinclude` results as `include`

- [x] 3.1 In `checkReferences` (`FhirController.java:1182`), change the `setUserData` value from
      `SearchEntryMode.MATCH` to `SearchEntryMode.INCLUDE`.
- [x] 3.2 Add a test: `GET /fhir/dev/ImmunizationRecommendation` with `_revinclude=Observation`
      against `RSP_Z42_MESSAGE` returns the recommendation with `search.mode == MATCH` and every
      retained `Observation` with `search.mode == INCLUDE`.
- [x] 3.3 Add a test: selecting entries with `search.mode == MATCH` from that same response yields
      only `ImmunizationRecommendation` resources — no `Observation`, no `Immunization`. (This is the
      assertion that depends on the v2tofhir history/forecast split; see 1.5.)
- [x] 3.4 Add a test: an `_include` naming a search name the conversion does not register succeeds
      with no additional entries and no error.

## 4. Fix 2 — no dangling references in a returned searchset

Implements `design.md` decisions 3, 4, and 5. Do 4.1 and 4.2 in one commit — 4.1 alone would leak
conversion-created resources into every response.

- [x] 4.1 Reduce `removeInfrastructureCreatedResources` to its white-list half: keep the
      `matchesSource` branch that marks `Resource:source:<type>` hits `INCLUDE`, drop the
      `it.remove()` at `:1120`, and drop the now-dead `Provenance` carve-out at `:1113`. Remove the
      `Iterator` parameter, which is no longer used. Removal now happens only in
      `cleanupBundleOfUnmarkedResources`.
- [x] 4.2 In `markIncludedResources`, after the two existing `checkReferences` calls, walk the
      resource's `References` set once more and, for each reference whose target is not already in
      `resources`, add it and mark it `SearchEntryMode.INCLUDE`. Guard against a null target
      (`ref.getUserData("Resource")` is null for any reference not built through
      `ParserUtils.toReference`). Do NOT walk `Reverses` — that is what keeps the forecast
      `Observation` resources out of a plain query.
- [x] 4.3 Add a test on `RSP_Z42_MESSAGE`, plain `GET /fhir/dev/ImmunizationRecommendation` with no
      `_include`: the `Patient` and the schedule `Organization` are present with
      `search.mode == INCLUDE`, neither is labelled `MATCH`, and `assertNoDanglingReferences` passes.
- [x] 4.4 Add a test on the same response: no `Observation` entry is present, and the returned
      `ImmunizationRecommendation` references none of the removed `Observation` resources.
- [x] 4.5 Add a test on `RSP_Z32_MESSAGE`, plain `GET /fhir/dev/Immunization`: every
      `Immunization.patient` resolves to a `Patient` entry in the bundle, and
      `assertNoDanglingReferences` passes.
- [x] 4.6 Add a test that the `_include=Resource:source:*` and `_include=Resource:source:<type>`
      white-list still retains unreferenced conversion-created resources with
      `search.mode == INCLUDE`, and that without it they are still absent. This pins the behaviour
      4.1 refactors around.
- [x] 4.7 Add a test that `OperationOutcome` entries survive with `search.mode == OUTCOME`, and that
      the bundle type is `SEARCHSET`. Cheap regression guard on the parts of `preFilter` this change
      moves code around in.

## 5. Measure the bundle-growth risk

`design.md` — Risks flags `/Immunization` growth as real and unmeasured. Resolve it before the PR is
reviewed, not after.

> **Measured against the real Nevada and Alaska captures** (`~/Downloads/ehex-testing`, driven
> through the mocked hub; the captures are deliberately NOT committed — they carry live vendor IIS
> patient demographics).
>
> *Superseded by section 7 for the recommendation path:* the "after" counts below predate the
> evaluated-history include, which takes NV from 5 to 13 and AK from 5 to 11. The `/Immunization`
> row is still current.
>
> | Capture / query | Entries before | Entries after | Dangling refs before | after |
> |---|---|---|---|---|
> | NV `/Immunization` (2 doses) | 4 | 11 | 6 | **0** |
> | NV `/ImmunizationRecommendation` (92 OBX) | 3 | 5 | 2 | **0** |
> | AK `/ImmunizationRecommendation` (88 OBX, 13 RXA) | 3 | 5 | 2 | **0** |
>
> The Nevada dangling reference reproduced byte-for-byte from the field notes:
> `ImmunizationRecommendation -> Patient/TlYwMDAwfDM5NzM1NjU`.
>
> Both recommendation queries return **exactly one** `ImmunizationRecommendation` (NV: 16
> components, AK: 10), `authority` = ACIP resolves in-bundle, and **0** of the 92/88 OBX
> Observations leak in. `/Immunization` returns exactly the 2 administered doses. 5.2 satisfied
> on every clause.
>
> `/Immunization` growth is the open trade-off: +7 entries for 2 doses (`Patient`, 4 `Location`,
> 2 `Organization`), i.e. roughly `3N + 3` added for N doses — about 4x on a 13-dose record. Every
> added resource is genuinely referenced by a match, but they are exactly the DatatypeConverter
> resources the original code deliberately dropped as "the enriched reference is enough". See 5.3.
>
> One design assumption did not survive contact: closure over v2tofhir's `References` user-data
> alone was **not** sufficient. `PractitionerRole -> Practitioner` is a reference v2tofhir does not
> register through `ParserUtils.toReference`, so it stayed dangling. The design's pre-authorised
> fallback (`clearUnresolvableReferences`) was implemented as a final sweep in `filter`, reducing
> any still-unresolvable reference to a logical reference with `identifier` + `display`. Without
> it the no-dangling-references guarantee does not hold.

- [x] 5.1 Against the `ehex-testing` captures (Nevada, Alaska), record entry counts before and after
      the change for `GET /Immunization` and `GET /ImmunizationRecommendation`, and put the numbers
      in the PR description. Expectation from the design: Z42 grows by ~2 entries; `/Immunization`
      grows by the distinct performer `Practitioner` and `Location` set.
- [x] 5.2 Run the end-to-end check from the field notes against those captures: `GET /Immunization`
      returns the administered doses, `GET /ImmunizationRecommendation` returns exactly one
      recommendation with one component per forecast, and no reference in either bundle points at a
      resource absent from that bundle.
- [x] 5.3 **Decided: not applied.** With the real numbers in (see above), the growth was judged
      acceptable — the added `Location` / `Organization` resources are genuinely referenced by a
      match, and returning them as resources rather than an inline `display` string is a gain for
      the client. The containment below remains the documented lever if a jurisdiction with very
      long immunization histories reports a payload-size problem.
      The containment, if ever needed: restrict the 4.2 closure to targets without a
      `Parser.SOURCE` marker and clear `Reference.reference` (keeping `identifier` and `display`)
      on the rest — a change to one predicate, since `clearUnresolvableReferences` already exists.

## 6. Docs, build gates, and review

- [x] 6.1 Document the searchset contract for API consumers: `match` vs `include` vs `outcome`, the
      `_include=Resource:source:<type>` white-list, and the guarantee that references resolve within
      the bundle. `docs/CONFIGURATION_REFERENCE.md` needs no change (no new property) — put this
      where the FHIR endpoints are described, or add a short section to `docs/QUICK_START.md`.
- [x] 6.2 Update the Newman/Postman collection in `testing/scripts/` if any request there asserts on
      `search.mode`, since `_include` / `_revinclude` results now report `include`. Check before
      editing — the collection may not exercise these parameters at all.
- [x] 6.3 Call the `match` -> `include` label change out as **BREAKING** in the PR description and
      release notes.
- [x] 6.4 Run `mvn clean package` and confirm Checkstyle passes at the `validate` phase — watch
      cyclomatic complexity on `markIncludedResources` (4.2 adds a loop) and `MultipleStringLiterals`
      on the `"Resource"` / `"References"` user-data keys, which are already repeated in this file;
      reuse or extract constants rather than adding another literal.
- [x] 6.5 Confirm the OWASP dependency-check gate still passes under CVSS 7. No dependency is added
      or changed by this work, so this is a no-op check unless the separate `v2tofhir` bump lands in
      the same branch.
- [x] 6.6 No security review needed: nothing here touches mTLS, JWT, `Roles`, `AccessControlValve`,
      or any BCFIPS crypto path. Confirm this still holds at review time — the change must not alter
      which resources a caller is authorised to see, only which are labelled and retained. Note that
      fix 2 does return resources (`Patient`, `Organization`) that were previously filtered out;
      confirm they come from the same IIS response the caller already receives and introduce no new
      data disclosure.

## 7. Include the Z42 evaluated history on a recommendation query

Added after the sections above were complete. A Z42 carries evaluated history that the `/Immunization`
path cannot reach, because that path sends Z34 and receives Z32, which lacks OBX `30973-2`,
`59782-3` and `59779-9`.

- [x] 7.1 In `preFilter`, mark `Immunization` entries `SearchEntryMode.INCLUDE` when the requested
      type is `ImmunizationRecommendation`. Keyed on `requested`, not on a plumbed-through
      `queryType` — the two are derived from the same URI, so no new parameter is needed.
- [x] 7.2 Extract the repeated "set mode, add to `resources`" into `markEntry`, and drop the
      `revIncludes` parameter `preFilter` no longer uses (dead since 4.1).
- [x] 7.3 Extend `RSP_Z42_MESSAGE` so the two administered doses carry the Z42-only evaluation OBX
      segments (`30956-7`, `30973-2`, `59782-3`, `59779-9`, `64994-7`).
- [x] 7.4 Tests: history returned as `include` and never `match`; `doseNumber` / `seriesDoses` /
      `authority` / `programEligibility` populated with `authority` resolving in-bundle; per-dose
      ORC-3 identifiers distinct with no `Type/id` collision; `/Immunization` still labels its doses
      `match`.
- [x] 7.5 Retarget `forecastObservationsCannotBeRevincluded` to
      `observationsArriveOnlyWhenRevincludedAndNeverAsMatch`. Two rounds here. First, including the
      history made history Observations reachable through their `partof` link to the now-included
      `Immunization` resources. Second, the current `v2tofhir` build also lets an explicit
      `_revinclude=Observation` reach the *forecast* Observations (15 retained on the fixture: 10
      history, 5 forecast), so the "nothing can revinclude a forecast Observation" claim no longer
      holds and the `partOf` assertion failed. That is correct behaviour, not a regression: the
      filter still never walks `Reverses` on its own (design decision 5), so the property worth
      pinning is the opt-in one — zero Observations by default, `include` and never `match` when
      the caller asks. The default-query assertion lives in
      `plainRecommendationQueryIsSelfContainedWithoutObservations`, unchanged.

> **Verified against the real captures.** No dangling references on any of the three.
>
> | Capture / query | Entries | Composition |
> |---|---|---|
> | NV Z42 `/ImmunizationRecommendation` | 13 | 1 IR `match`; `include`: 2 Immunization, 1 Patient, 3 Organization, 4 Location; 2 outcome |
> | AK Z42 `/ImmunizationRecommendation` | 11 | 1 IR `match`; `include`: 3 Immunization, 1 Patient, 2 Organization, 2 Location; 2 outcome |
> | NV Z32 `/Immunization` | 11 | unchanged from section 5 — 2 Immunization `match` |
>
> NV doses carry the expected `NV0000|41348935` and `NV0000|41348937`, `doseNumber` = 1, and
> `programEligibility`. Both doses' `protocolApplied.authority` and the recommendation's
> `authority` resolve to the *same* ACIP `Organization` (`VXC16`) — v2tofhir dedupes it across all
> 18 `59779-9` occurrences.
