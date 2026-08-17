## 0. Capture the pre-change baseline

This group runs first, while the auto-retain code is still in place. Task 4.3 asserts against what it
records, so it cannot be taken after the deletions in groups 1 and 2.

- [x] 0.1 Run the Z42 test message through `GET .../ImmunizationRecommendation` with no query
      parameters and record, per entry, the resource type and `search.mode`. Keep the record in the
      test class as the expected value for the round-trip test, not as a checked-in JSON fixture, so
      it stays readable next to the assertion that uses it.

      Recorded, 7 entries: `OperationOutcome` x2 `outcome`, `ImmunizationRecommendation` x1 `match`,
      `Patient` x1 `include`, `Immunization` x2 `include`, `Organization` x1 `include`. No
      `Location`, manufacturer `Organization`, or performer `Practitioner` — the Z42 fixture's
      `ORC`/`RXA` segments carry no RXA-10 performer and no RXA-11 facility, so the conversion never
      creates them.
- [x] 0.2 Do the same for the Z32 test message through `GET .../Immunization` with no query
      parameters, and additionally record which references have no `reference` element — the
      `PractitionerRole` to `Practitioner` case that task 4.5 inverts.

      Recorded, 10 entries: `OperationOutcome` x2 `outcome`, `Immunization` x2 `match`, `Patient` x1
      `include`, `PractitionerRole` x2 `include`, `Practitioner` x1 `include`, `Location` x2
      `include`. Five references currently have their `reference` element cleared — four on the two
      `PractitionerRole` resources and one on a `Location`. Only two of the five retain any readable
      content (`display = "Carl Clinician"`); the other three carry neither `identifier` nor
      `display`, so today they ship as empty `Reference` objects. That is a stronger argument for
      this change than the proposal claims, and task 4.5 asserts against these numbers.

## 1. Remove the unrequested-retain behavior

- [x] 1.1 In `FhirController.java`, delete the `retainReferencedResources(resources, refs)` call from
      `markIncludedResources` and delete the `retainReferencedResources` method with its Javadoc.
- [x] 1.2 Delete the `r instanceof Immunization && IMMUNIZATION_RECOMMENDATION.equals(requested)`
      branch from `preFilter`, so the method marks only the requested type and `OperationOutcome`
      before falling through to `whitelistInfrastructureCreatedResources`.
- [x] 1.3 Rewrite the `preFilter` Javadoc: drop the Z42 evaluated-history rationale paragraph and
      state the strict rule instead, noting that the evaluated history and its OBX-derived data are
      now reached with `_include=ImmunizationRecommendation:patient` plus
      `_revinclude=Immunization:patient`.
- [x] 1.4 Confirm `IMMUNIZATION_RECOMMENDATION` is still referenced by `addSearchableResource` and
      that the `org.hl7.fhir.r4.model.Immunization` import is still used by the `subject`-to-`patient`
      aliasing and identifier validation, so neither is removed.

## 2. Remove the reference stripping

- [x] 2.1 Delete the `clearUnresolvableReferences(bundle)` call from `filter`, so `filter` ends at
      `cleanupBundleOfUnmarkedResources`.
- [x] 2.2 Delete `clearUnresolvableReferences`, `toRelativeReference`, and `forEachReference` with
      their Javadoc.
- [x] 2.3 Remove the now-unused imports `java.util.HashSet`, `java.util.function.Consumer`, and
      `org.hl7.fhir.r4.model.Property`. Confirm `RESOURCE_KEY` is still used by `checkReferences`
      and that `java.util.Set` is still used by the `References` / `Reverses` handling.
- [x] 2.4 Confirm `checkReferences` still sets `SearchEntryMode.INCLUDE` on an `_include` /
      `_revinclude` hit — this labelling is deliberately kept, not reverted.
- [x] 2.5 Confirm `whitelistInfrastructureCreatedResources` still removes nothing and that all
      removal remains in the single `cleanupBundleOfUnmarkedResources` pass.

      Verified, and one assumption corrected: a bare `_revinclude=Provenance` does **not** reach the
      conversion-created `Provenance`, and did not on `develop` either.
      `whitelistInfrastructureCreatedResources` marks the resource but never adds it to `resources`,
      so `markIncludedResources` never traverses it, and `develop`'s carve-out set no mode so cleanup
      deleted it anyway. Measured: `_revinclude=Provenance` gives 4 entries with no `Provenance`;
      `_include=Resource:source:DocumentReference&_revinclude=Provenance` gives 5, the
      `DocumentReference` arriving and the `Provenance` still not. Recorded in design.md.

## 3. Invert the branch's unit tests

- [x] 3.1 In `FhirControllerTests.java`, delete the `assertNoDanglingReferences` and
      `collectReferences` helpers and the `org.hl7.fhir.r4.model.Base` import they need.
- [x] 3.2 Replace `recommendationQueryRetainsPatientAndAuthorityOrganization` with a test asserting
      that a plain `GET .../ImmunizationRecommendation` returns neither the `Patient` nor the
      schedule `Organization`, and a second test asserting both arrive with
      `search.mode = "include"` when `_include=ImmunizationRecommendation:patient` and
      `_include=ImmunizationRecommendation:authority` are supplied.
- [x] 3.3 Replace `recommendationQueryIncludesTheEvaluatedHistory` with a test asserting no
      `Immunization` entry appears on a plain recommendation query.
- [x] 3.4 Rework `includedHistoryCarriesTheZ42OnlyEvaluationData` and
      `includedHistoryHasStableDistinctIdentifiers` to send
      `_include=ImmunizationRecommendation:patient&_revinclude=Immunization:patient`, then keep their
      existing assertions on `protocolApplied.doseNumber`, `seriesDoses`, `protocolApplied.authority`,
      `programEligibility`, and per-dose ORC-3 identifiers.
- [x] 3.5 Replace `immunizationQueryResolvesItsPatientReference` with a test asserting that on a
      plain `GET .../Immunization` the `Patient` is absent and every `Immunization.patient` keeps the
      literal `reference` value the conversion produced, with `identifier` and `display` unchanged.
- [x] 3.6 Delete `unretainableTargetIsReducedToALogicalReference` and replace it with a test
      asserting no `Reference` in a returned searchset has had its `reference` element cleared. Task
      4.5 covers the specific `PractitionerRole` case this test used, so do not duplicate it here.
- [x] 3.7 Narrow `plainRecommendationQueryIsSelfContainedWithoutObservations` to the `Observation`
      absence assertion, dropping the self-containment claim about references.
- [x] 3.8 Run the tests kept unchanged and confirm they still pass:
      `revincludedObservationsAreLabelledIncludeNotMatch`, `selectingMatchYieldsOnlyTheRequestedType`,
      `observationsArriveOnlyWhenRevincludedAndNeverAsMatch`, `unmatchedIncludeParameterIsNotAnError`,
      `immunizationQueryStillReturnsHistoryAsMatch`,
      `conversionCreatedResourcesStillNeedWhitelistingWhenUnreferenced`,
      `outcomesSurviveAndBundleIsASearchset`, `namedTypeWhitelistRetainsOnlyThatType`,
      `partOfRevincludeNarrowsToTheHistoryObservations`, `matchOperationLabelsThePatientAsMatch`.

## 4. Add unit tests for the new requirements

- [x] 4.1 Add a test for "a plain immunization query returns immunizations only": on the Z32 fixture
      the searchset holds exactly 4 entries — `Immunization` x2 as `match` and `OperationOutcome` x2
      as `outcome` — down from the 10 recorded in task 0.2. Assert `Patient`, `PractitionerRole`,
      `Practitioner`, and `Location` are all absent.
- [x] 4.2 Add a round-trip test proving `_include=*:*&_revinclude=Immunization` reproduces the task
      0.1 baseline exactly: 7 entries, `ImmunizationRecommendation` x1 as `match`,
      `OperationOutcome` x2 as `outcome`, and `Patient` x1, `Immunization` x2, `Organization` x1 as
      `include`. Compare the full type-and-mode multiset against the recorded baseline rather than
      spot-checking types, so a regression in either direction fails. This is the parameter pair
      task 5.4 documents as the way to recover the old payload, and nothing currently tests
      `_include=*:*` at all despite `docs/fhir/fhir-api.md` advertising it.
- [x] 4.3 Add a test that `_include=*:*&_revinclude=Immunization` still returns no `Observation`
      entry, matching the pre-change payload — `retainReferencedResources` never walked the reverse
      direction, and naming only `Immunization` in the `_revinclude` preserves that.
- [x] 4.4 Add a test that `_include=*:*` alone, with no `_revinclude`, returns no `Immunization` on a
      recommendation query, since the evaluated history is reachable only in reverse. This pins why
      the second parameter is not optional. Note that the task 0.1 baseline run cannot pre-verify
      this: with the auto-retain still in place, `_include=*:*` alone returns the same 7 entries,
      because `preFilter` supplies the `Immunization` regardless of any parameter. The assertion is
      only meaningful after the deletions in group 1.
- [x] 4.5 Add a test for the cleared references on the Z32 immunization path — the case
      `unretainableTargetIsReducedToALogicalReference` covered. Under `_include=*:*` the
      `PractitionerRole` and `Location` resources are retained, and the two references that carried a
      `display` now keep the literal value the conversion produced.

      Correcting task 0.2: of the five references recorded without a `reference` element, only two
      were stripped by `clearUnresolvableReferences`. The other three carry no `reference`, no
      `identifier`, and no `display` as v2tofhir produces them, and they stay empty after the
      deletion. Do not assert that no empty `Reference` exists — assert instead that the count of
      references lacking a literal value dropped from five to three, so the test pins what this
      change actually did.
- [x] 4.6 Add a test that a `patient` reference carries the same value whether or not
      `_include=Immunization:patient` retained the target, and resolves to the retained `Patient`
      when it did.
- [x] 4.7 Add a test pinning that `_revinclude` does not reach a white-listed resource:
      `_revinclude=Provenance` alone retains no `Provenance`, and adding
      `_include=Resource:source:DocumentReference` retains the `DocumentReference` but still no
      `Provenance`. Assert both requests succeed. This replaces the assumption corrected in task 2.5.
- [x] 4.7a Add a test for the general rule that a reverse include resolves only from a retained
      resource: on the Z42 recommendation path `_revinclude=Observation` alone retains no
      `Observation`, and adding `_include=ImmunizationRecommendation:patient` makes the same
      `_revinclude` retain them as `include`.
- [x] 4.8 Add a Z42 fixture variant carrying RXA-10 performer and RXA-11 facility on the two
      administered doses, so the conversion produces `Location` and performer resources the existing
      `RSP_Z42_MESSAGE` does not. Task 0.1 established the current fixture yields no `Location` at
      all, so the four-hop chain cannot be tested without this. Add it as a separate constant and
      leave `RSP_Z42_MESSAGE` untouched, so no existing entry-count assertion moves.
- [x] 4.9 Using that fixture, add a test for the four-type query a caller actually sends —
      `_include=ImmunizationRecommendation:patient&_include=ImmunizationRecommendation:authority&_revinclude=Immunization&_include=Immunization:location`
      — asserting `Patient`, `Organization`, `Immunization`, and `Location` all arrive as `include`
      and no `Observation` does. This exercises the four-hop chain through the growing `resources`
      list (recommendation to `Patient` and `Organization` forward, `Patient` to `Immunization`
      reverse, `Immunization` to `Location` forward) in a single pass. Add a second assertion running
      the same four parameters in reversed URL order and comparing the two searchsets, confirming
      parameter order does not matter.
- [x] 4.10 Add a test that dropping `_include=ImmunizationRecommendation:patient` from the task 4.9
      query returns no `Immunization`, since the reverse hit is found on the `Patient` and the
      `Patient` must be retained first.
- [x] 4.11 Add a test that `Organization` and `Location` arrive under an ordinary `_include` without
      any `_include=Resource:source:...` parameter, even though both are conversion-created
      (`Parser.SOURCE`). This is the behavior the modified white-list requirement pins.
- [x] 4.12 Add a test that organization configuration does not affect the returned resource types:
      run the same query for two organizations whose pipelines apply different transformations and
      assert the two searchsets hold the same set of resource types and search modes.
- [x] 4.13 Remove the temporary `zzzTemporaryBaselineDump` test and its `dumpBaseline` helper, added
      only to capture the group 0 baseline.
- [x] 4.14 Run the full class via Maven so the surefire env vars and `target/` BCFKS keystores are in
      place: `mvn test -Dtest=FhirControllerTests`. Note that the Mockito inline mock maker needs JVM
      self-attach, so this cannot run inside a restricted sandbox.

## 5. Update documentation

- [x] 5.1 Rewrite the searchset section of `docs/fhir/fhir-api.md`: state that a query returns the
      requested type plus `OperationOutcome` only, correct the claim that a referenced resource is
      returned with the records that reference it, correct the claim that `_include=Immunization:patient`
      and `_include=Immunization:performer` "add nothing", and remove the passage describing a
      reference reduced to `identifier` and `display`.
- [x] 5.2 Rewrite the filtering and search-mode sections of `docs/fhir/rsp-to-fhir.md`: redefine
      `include` as an `_include` / `_revinclude` hit or a `Resource:source` white-list hit only, and
      drop "a resource a match references".
- [x] 5.3 Add a worked Z42 example to `docs/fhir/rsp-to-fhir.md` showing
      `_include=ImmunizationRecommendation:patient&_revinclude=Immunization:patient&_include=Immunization:authority`,
      the entry modes it produces, and which OBX codes (`30973-2`, `59782-3`, `59779-9`, `64994-7`)
      the included `Immunization` carry — the discoverability mitigation from design.md. State that
      the `_include` on `patient` is not optional: the `Immunization` reference the `Patient`, not the
      recommendation, so the `_revinclude` finds nothing without the `Patient` retained. State that
      `_include=Immunization:authority` is what resolves `protocolApplied.authority` inside the
      searchset, because that `Organization` is registered on the `Immunization` and not on the
      `ImmunizationRecommendation`.
- [x] 5.3a Document the general rule in `docs/fhir/rsp-to-fhir.md`: a `_revinclude` resolves only
      from a resource already in the searchset, so reaching a resource that references another
      non-requested resource takes a forward `_include` first. Note that `_revinclude` never reaches
      a resource retained only by the `Resource:source` white-list.
- [x] 5.4 Add a migration note to `docs/fhir/fhir-api.md` for callers upgrading from the pre-change
      behavior: `_include=*:*&_revinclude=Immunization` on an `ImmunizationRecommendation` query
      returns the same entry set the service returned before, with the one difference that a
      reference built outside the conversion's bookkeeping — `PractitionerRole` to `Practitioner` —
      now keeps its literal value rather than being reduced to `identifier` and `display`. Note that
      `_include=*:*` alone does not return the evaluated history, because it is reachable only in
      reverse.
- [x] 5.5 Document the search-parameter names a caller can actually use, since an unregistered name
      silently matches nothing: `ImmunizationRecommendation:patient`,
      `ImmunizationRecommendation:authority`, `Immunization:patient`, `Immunization:authority`,
      `Immunization:location`, `Immunization:performer`, `Immunization:manufacturer`,
      `Observation:partof` / `part-of`, and `Resource:source:<type>`. Note which are forward-only.
- [x] 5.6 Document that a reference in a returned searchset may point outside it and that this
      service serves no read endpoint for the target, so the caller resolves it with `_include` or
      against its own data using `identifier` and `display`.
- [x] 5.7 Confirm no change is needed in `docs/CONFIGURATION_REFERENCE.md`,
      `docs/APPLICATION_CONFIGURATION_STORAGE.md`, or `docs/QUICK_START.md` — this change adds no
      runtime property, touches no configuration entity, and alters no `curl` example there.

## 6. Update the integration test collection

- [x] 6.1 Confirm the existing `TS_TC_07*` / `TS_TC_08*` FHIR cases in
      `testing/scripts/TS_Integration_Test.postman_collection.json` still pass — they assert only
      `Bundle` type, at least one entry, and `Immunization` presence with vaccine code `208`, none of
      which this change alters.
- [x] 6.2 Add a case asserting that a plain FHIR `Immunization` query returns no `Patient` entry.
- [x] 6.3 Add a case sending `_include=Immunization:patient` and asserting the `Patient` is present
      with `entry.search.mode == "include"`.
- [x] 6.4 Add a case sending `_include=*:*&_revinclude=Immunization` against
      `ImmunizationRecommendation` and asserting `Patient`, `Organization`, and `Immunization` are all
      present as `include` with the recommendation the only `match` — the recovery recipe documented
      in task 5.4, exercised against a live IIS rather than a fixture.
- [x] 6.5 Mirror the three new cases into the JWT Okta folder, matching how the existing FHIR cases
      are duplicated there.

## 7. Build gates and review

- [x] 7.1 Run `mvn clean package -DskipDependencyCheck=true` and confirm Checkstyle passes at the
      `validate` phase, with particular attention to `UnusedImports` after the deletions in tasks 1
      and 2.
- [x] 7.2 Confirm no `@JsonSubTypes` registration is needed: this change adds no `Operation` or
      `Precondition` subclass, so `Operation.java` and `Precondition.java` are untouched.
- [x] 7.3 Confirm no security review is required: no `AccessControlValve`, `Roles`, `XformPrincipal`,
      mTLS, JWT, BouncyCastle FIPS, or BCFKS keystore code is touched, and no authorization decision
      depends on searchset assembly.
- [x] 7.4 Run `mvn verify` and confirm the OWASP dependency-check result is unchanged and stays under
      CVSS 7 — this change adds and removes no dependency.
- [x] 7.5 Delete the untracked scratch file
      `src/test/java/gov/cdc/izgateway/xform/endpoints/fhir/AkZ42Scratch.java` before the branch
      merges.
- [ ] 7.6 Notify the eHealth Exchange pilot contact of the `_include` / `_revinclude` parameters they
      must add, before the merge to `develop` triggers the dev ECS force-new-deployment.

## 8. Close out the change

- [x] 8.1 Run `openspec validate fhir-searchset-strict-includes --strict` and confirm it passes.
- [ ] 8.2 Run `/opsx:verify` to confirm the implementation matches the delta spec, then archive with
      `/opsx:archive` so `openspec/specs/fhir-searchset-filtering/spec.md` absorbs the two added
      requirements, the two modified ones, and the two removals.
