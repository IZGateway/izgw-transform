## Context

See `proposal.md` — Why. This change is confined to the searchset assembly step inside
`FhirController.filter(Bundle, HttpServletRequest)`. That step runs after the response has already
come back from the downstream Hub or IIS, been converted by `MessageParser`, and been carried back
through the transformation pipeline. Nothing upstream of it is involved.

Two facts shape the approach:

**The target behavior already shipped.** `develop` returns exactly the strict contract this change
restores: `preFilter` marks the requested type `match` and `OperationOutcome` `outcome`, everything
else falls through to `cleanupBundleOfUnmarkedResources`, and an unresolved reference keeps its
literal value because no code touches it. The `IGDD-3285` branch added three things on top —
`retainReferencedResources`, `clearUnresolvableReferences`, and a Z42 `Immunization` branch in
`preFilter` — and those three are what this change removes. So this is a subtraction, not a design
problem, and the risk profile is that of reverting to code that has been in `develop` all along.

**One branch change is worth keeping, and one is worth keeping for a reason `develop` did not
have.** The `include`-not-`match` labelling of join hits (`checkReferences`) is a genuine fix and
stays. Separately, the branch restructured removal: `develop` removed conversion-created resources
eagerly inside `preFilter` with `Iterator.remove()`, whereas the branch marks nothing and lets the
single `cleanupBundleOfUnmarkedResources` pass do all removal. That restructuring stays too — see
Decisions.

## Goals / Non-Goals

**Goals:**

- Delete the three additions so the returned entry set matches `develop` exactly.
- Keep the branch's `include` labelling and its single-cleanup-pass structure.
- Leave every `Reference` untouched, including mandatory 1..1 references whose target is omitted.
- Keep `filter` and its helpers within Checkstyle's complexity and length limits, which subtraction
  makes easier, and clean up the imports the deletions strand.

**Non-Goals:**

- No configuration switch to select strict or lenient assembly. See Decisions.
- No convenience so a bare `_revinclude=Immunization` reaches the Z42 evaluated history through the
  `Patient` without the `Patient` being retained. The caller sends both parameters.
- No folding of `_include=Resource:source:*` into a bare `_include=*:*`. The two parameters keep
  their present, separate meanings.
- No read endpoint for a reference target. A literal reference in a returned searchset stays
  unfetchable from this service, and the spec says so.
- No change to `MessageParser`, to `v2tofhir` reference bookkeeping (`References`, `Reverses`,
  `SEARCH_NAMES`, `REVERSE_NAMES`, `Parser.SOURCE`), or to the CapabilityStatement.

## Decisions

### Subtract the three additions rather than gate them behind a property

A property such as `xform.fhir.strict-searchset` would let the eHealth Exchange pilot keep the
current behavior while new callers get the strict contract.

Rejected. A FHIR response shape is part of the API contract, and a deployment-time switch means two
contracts that no caller can discover — the CapabilityStatement does not advertise it, and a caller
cannot tell from a response which mode produced it. It also doubles the assembly paths under test
forever. The pilot's migration is adding `_include` parameters to queries it already sends, which is
smaller than the cost of carrying the switch.

Alternative considered and rejected: keep the auto-retain but drop the retained resources' own
`match`-adjacent visibility some other way. There is no such way — the entry is either in the bundle
or not.

### Deliver a reference exactly as the conversion produced it, empty or not

Removing the stripping does not make every reference readable. Of the five references the pre-change
code shipped without a `reference` element on the Z32 fixture, only two were stripped by
`clearUnresolvableReferences`; the other three carry no `reference`, no `identifier`, and no `display`
as `v2tofhir` produces them, and they stay that way after the deletion.

This change does not attempt to populate them. Doing so would mean synthesising content the
conversion did not produce, which is the conversion's concern, not the searchset's. The delta spec
says so explicitly, so a later reader does not mistake an empty reference for a regression this
change introduced.

### Delete `clearUnresolvableReferences` rather than keep it as a narrow fallback

The branch's fallback — strip `reference`, keep `identifier` and `display` — could be kept for the
narrow case it was written for: a reference built directly with `new Reference(...)` rather than
through `ParserUtils.toReference`, which v2tofhir never registered, so no target resource exists to
retain.

Rejected. Once nothing is auto-retained, that case is no longer distinguishable from the ordinary
one: after this change, *most* references point outside the searchset, so a rule that strips
unresolvable references would strip nearly all of them. And stripping discards information — the
conversion produced that reference value, and a caller correlating a response against its own data
may want it. `develop` shipped the literal value in both cases and no defect was raised against
that. Deleting the method and its `toRelativeReference` and `forEachReference` helpers is therefore
the smaller contract, not just the smaller diff.

### Keep the branch's single-cleanup-pass removal, not `develop`'s eager removal

`develop`'s `removeInfrastructureCreatedResources` called `it.remove()` on a conversion-created
resource during `preFilter`, before include marking had run. That forced a special case: a
`MessageParser`-sourced `Provenance` was spared from removal when `_revinclude` named `Provenance`.
The branch's `whitelistInfrastructureCreatedResources` removes nothing; it only marks, and all
removal happens once in `cleanupBundleOfUnmarkedResources` after include marking. Keep the branch's
version: one removal point is easier to reason about than removal split across two passes.

The carve-out is dropped because it was dead code: `develop` only skipped the `it.remove()` without
setting a `search.mode`, so `cleanupBundleOfUnmarkedResources` deleted the `Provenance` on the next
pass regardless. `_revinclude=Provenance` returned nothing on `develop` and returns nothing now —
measured on the Z32 fixture at matched v2tofhir versions — so removing the carve-out changes no
observable behavior.

A white-listed resource **is** traversed. `whitelistInfrastructureCreatedResources` adds it to the
`resources` list, so `markIncludedResources` visits it and walks its reverse references like any
other retained resource. The reason `_revinclude=Provenance` fails is unrelated to white-listing —
see the known limitation below.

### Known limitation: a type-qualified `_revinclude` cannot match `Provenance`

`_revinclude=Provenance:target` is the standard FHIR way to ask for the provenance of search results,
and it returns nothing here. This is a pre-existing defect, unchanged by this work, and it is not in
`FhirController`.

Measured on the Z32 fixture, with the `DocumentReference` that the `Provenance` resources reference
white-listed so it is retained and traversed:

```
_include=Resource:source:DocumentReference & _revinclude=*:*         -> Provenance x23 retained
_include=Resource:source:DocumentReference & _revinclude=Provenance  -> none retained
```

Same retained resources, same traversal, different only in the type check. `includeMatches` compares
the parameter's type against `ref.getReferenceElement().getResourceType()`, and for `Provenance` that
is `null`, so `"Provenance".equals(null)` fails. `*` short-circuits the check, which is why the
wildcard form works.

The null comes from v2tofhir. `Parser.addResource` assigns every resource a type-qualified id with
`new IdType(resource.fhirType(), id)`, but the auto-created `Provenance` is added straight to the
bundle with `p.setId(getIdGenerator().get())`, bypassing that line. Probing the converted bundle,
`Provenance` is the only one of fourteen resource types with a bare id; the other thirteen are all
type-qualified, which is why `_revinclude=Immunization` and `_revinclude=Observation` do work.

Two candidate fixes, neither in scope here:

1. **v2tofhir** — give the auto-created `Provenance` a type-qualified id like every other resource.
   Fixes it for every consumer, and removes the inconsistency at its source.
2. **xform, defensively** — have `includeMatches` compare against the target resource's `fhirType()`,
   which it already holds via `ref.getUserData(RESOURCE_KEY)`, instead of parsing the type out of the
   reference string. Robust regardless of id shape, and works against older v2tofhir.

Both are behavior additions rather than part of restoring the strict contract, so they belong in their
own change. The delta spec therefore states the general rule and records this as a limitation rather
than specifying the current behavior as intended.

### A reverse include resolves only from a retained resource

This is not a decision so much as a consequence that had to be discovered and then written down.
`ParserUtils.toReference(target, source, names)` records the forward reference on the source and a
reference **to the source** in the target's `Reverses` set. A `_revinclude` is therefore resolved by
traversing the resource being pointed at — so it fires only when that resource is already in the
retained set.

Once the auto-retain is gone, this changes observable behavior on the recommendation path, because
the evaluated-history `Immunization` and the forecast `Observation` both reference the `Patient`
rather than the `ImmunizationRecommendation`. Measured on the Z42 fixture: `_revinclude=Observation`
alone returns 0 `Observation`; with `_include=*:*` added, which retains the `Patient`, it returns 15.
The same applies to `_revinclude=Immunization`, which is why the documented recovery recipe pairs it
with a forward `_include`.

One further consequence: the schedule `Organization` behind `protocolApplied.authority` is registered
on the `Immunization` (`OBXParser` line 478), not on the `ImmunizationRecommendation`, so
`_include=Immunization:authority` is what retains it. `_include=ImmunizationRecommendation:authority`
retains the separate `Organization` the forecast itself points at (`OBXParser` line 631). A caller
wanting the evaluation data resolvable inside the searchset needs the former.

### Retain `IMMUNIZATION_RECOMMENDATION`, `RESOURCE_KEY`, and the `Immunization` import; drop the rest

Deleting the Z42 branch removes the only use of `IMMUNIZATION_RECOMMENDATION` inside `filter`, but
the constant is still used by `addSearchableResource` in the CapabilityStatement, and the
`Immunization` import is still used by the `subject`-to-`patient` parameter aliasing and by
identifier validation. Both stay. `RESOURCE_KEY` loses its use in `retainReferencedResources` but
keeps the one in `checkReferences`.

`Property` and `java.util.function.Consumer` are used only by `forEachReference`, and
`java.util.HashSet` only by `clearUnresolvableReferences`. All three imports must go with the
deletions or Checkstyle's `UnusedImports` module fails the build at the `validate` phase.

One constant is added rather than retained. `matchesSource` compared the `_include=Resource:source:…`
parameter's type against the literal `"Resource"`, which SonarQube flagged as duplicating
`RESOURCE_KEY`. They spell the same by coincidence and must not be merged: `RESOURCE_KEY` is an
internal v2tofhir user-data key that v2tofhir may rename, while the literal in `matchesSource` is a
token in the caller's URL, documented in `docs/fhir/rsp-to-fhir.md`. Sharing one constant would let a
rename of either silently change the other with no compile error, so the URL token gets its own
`SOURCE_INCLUDE_TYPE`.

### No pipeline, transport, or persistence involvement

Searchset assembly reads only the converted `Bundle` and the inbound `HttpServletRequest` query
parameters. It consults no repository, so neither the file nor the DynamoDB backend is touched and
`SPRING_DATABASE=migrate` has nothing to migrate. It reads no organization configuration, so no
`Organization`, `Pipeline`, `Solution`, `Operation`, or `Precondition` changes and no existing
configuration is invalidated. It performs no crypto, so the BouncyCastle FIPS providers and BCFKS
keystores are not implicated. It is not annotated `@CaptureXformAdvice` and is not a
`SolutionOperation`, so the AspectJ advice and the `aspectjweaver` / `spring-instrument` javaagents
see no change — `PipelineAdvice` records the transformations that ran, which is upstream of
assembly.

### Flow

Assembly sits at the end of the existing response path and is unchanged in position:

```
Client                FhirController         XformRouter/Camel        Downstream (izghub|iis)
  |                        |                       |                          |
  | GET /fhir/{dest}/Immunization?_include=...     |                          |
  |----------------------->|                       |                          |
  |                        | build HL7 v2 Z34/Z44  |                          |
  |                        |---------------------->|                          |
  |                        |                       | REQUEST-direction pipes  |
  |                        |                       |------------------------->|
  |                        |                       |   RSP (Z32/Z42)          |
  |                        |                       |<-------------------------|
  |                        |                       | RESPONSE-direction pipes |
  |                        |                       | (reverse pipe order)     |
  |                        |<----------------------|                          |
  |                        | MessageParser.convert -> Bundle (type=message)   |
  |                        |                                                  |
  |                        | filter(bundle, req):                             |
  |                        |   type = searchset                               |
  |                        |   preFilter        -> match / outcome            |
  |                        |   markIncludedResources -> include (per _include)|
  |                        |   cleanupBundleOfUnmarkedResources -> remove rest|
  |                        |   (references left exactly as converted)         |
  |<-----------------------|                                                  |
  |   searchset Bundle     |                                                  |
```

With `x-loopback: true` the downstream call is short-circuited and the transformed request is
returned directly, so `filter` is not reached — loopback tests exercise the pipeline, not assembly.
The RESPONSE-direction reverse pipe ordering happens strictly before conversion, so pipe order has
no bearing on which entries survive assembly.

### Error handling

Assembly stays non-throwing. A missing or malformed `_include` value already resolves through
`normalizeInclude` to wildcards rather than raising, and an `_include` naming a type or search name
the conversion never registered simply matches nothing — the delta spec keeps that behavior, and the
existing "an unmatched include parameter is not an error" scenario in the main spec still governs it.
The deletions remove code paths, so they introduce no new failure mode; conversion warnings continue
to reach the caller as `OperationOutcome` entries with `search.mode = "outcome"`, which no part of
this change touches.

## Risks / Trade-offs

- **The eHealth Exchange pilot is already exposed to the strict behavior** → The branch has not
  merged to `develop`, but CI deploys it to the dev ECS cluster, and the Newman case `TS_TC_07e`
  passes there. That case asserts `search.mode = "include"` on an `_include` hit, which pre-change
  code labelled `match`, so `dev.xform.izgateway.org` is demonstrably serving this branch. The
  notification is therefore overdue rather than pre-emptive: give the pilot contact the parameter list
  from the proposal's What Changes section, and confirm they read `Patient` from an `_include` they
  send rather than from an unrequested entry.
- **Z42 evaluation data becomes hard to discover** → It is reachable through no other call, and the
  three-parameter combination that reaches it in full is not something a caller would guess:
  `_include=ImmunizationRecommendation:patient` to anchor the reverse lookup,
  `_revinclude=Immunization:patient` for the doses, and `_include=Immunization:authority` so
  `protocolApplied.authority` resolves inside the searchset — that `Organization` is registered on the
  `Immunization`, not on the recommendation, as recorded above. Mitigation is documentation, not code:
  `docs/fhir/rsp-to-fhir.md` carries a worked example showing all three parameters, the resulting
  entry modes, and which OBX codes the included `Immunization` carry.
- **A caller doing local reference resolution inside the bundle now fails to resolve
  `Immunization.patient`** → This is the defect the branch set out to fix, and this change
  reinstates it deliberately: the reference is delivered with its `identifier` and `display`, and a
  caller that needs the target in the bundle sends `_include`. Recorded in the delta spec's
  **Migration** note for the removed requirement so the decision is not relitigated as a bug.
- **Deleting methods strands imports and fails the build late** → `UnusedImports` fires at the
  `validate` phase, before compilation, so a stranded `Property`, `Consumer`, or `HashSet` import
  fails fast rather than reaching CI. Task list carries an explicit Checkstyle verification step.
- **Test assertions written for the branch behavior invert rather than disappear** → The
  `FhirControllerTests` cases added on the branch assert auto-retained `Patient` / `Organization`,
  Z42 history, and stripped references. Each has a strict counterpart — the resource absent by
  default, present when requested, and the reference intact — so the fixtures are reused and only
  the assertions change. Deleting them outright would lose coverage of the new contract.

## Migration Plan

1. Land the deletions and the test inversion on `IGDD-3285` before the branch merges to `develop`,
   so `develop` never carries the auto-retain behavior.
2. Update `docs/fhir/fhir-api.md` and `docs/fhir/rsp-to-fhir.md` in the same commit range as the
   code, since both currently document the auto-retain and the reference stripping as the contract.
3. Notify the eHealth Exchange pilot with the `_include` / `_revinclude` parameter list. This is
   overdue rather than pending: CI has already deployed the branch to the dev ECS cluster, so the
   pilot is seeing the strict behavior on `dev.xform.izgateway.org` now. Do it before the merge to
   `develop` promotes it further.
4. Rollback: revert the change commits. There is no data migration, no persisted state, and no
   configuration to undo, so rollback is a redeploy of the previous image.
