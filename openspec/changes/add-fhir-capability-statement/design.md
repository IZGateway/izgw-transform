## Context

xform's FHIR surface is a hand-rolled Spring `@RestController` (`gov.cdc.izgateway.xform.endpoints.fhir.FhirController`, base path `@RequestMapping("/fhir")`), not a HAPI `RestfulServer`. It exposes `GET /fhir/{destinationId}/{Patient|Immunization|ImmunizationRecommendation}` searches and `POST /fhir/{destinationId}/Patient/$match`, but publishes no `/metadata` conformance document. FHIR R4 requires servers to serve a `CapabilityStatement` at `[base]/metadata`, and — more pressingly — DIBBs Query Connector only turns on `Patient/$match` when it fetches `/metadata` and finds a `Patient` operation named `match`.

Relevant current-state facts that shape this design:
- FHIR resources returned from the controller (`Bundle`, `Resource`, `OperationOutcome`) are serialized by a project-wide `FhirConverter` `HttpMessageConverter` registered in `Application.configureMessageConverters(...)` ahead of the Jackson converters. Returning a HAPI R4 resource from a handler is the established serialization path.
- The controller is class-annotated `@RolesAllowed({XFORM_SENDING_SYSTEM, ADMIN})` and calls `AccessControlRegistry.register(this)` in its constructor; access control is path/role based via `AccessControlValve` (mTLS).
- HAPI R4 model classes (`org.hl7.fhir.r4.model.*`) are already imported and used throughout the controller; `org.hl7.fhir.r4.model.CapabilityStatement` is available on the classpath (no new dependency).
- Checkstyle fails the build at `validate`: method length <= 300, bounded cyclomatic complexity, and `MultipleStringLiterals` (max 3–4 repeats).

Stakeholders: the eHealth Exchange / DIBBs immunization pilot (needs `$match` auto-detection), plus any FHIR client expecting R4-conformant behavior.

## Goals / Non-Goals

**Goals:**
- Add `GET /fhir/{destinationId}/metadata` returning a valid FHIR R4 `CapabilityStatement` (HTTP `200`, `application/fhir+json`).
- Advertise `fhirVersion 4.0.1`, `format` including `application/fhir+json`, and the `Patient`/`Immunization`/`ImmunizationRecommendation` resources with the `search-type` interaction.
- Advertise the `Patient/$match` operation (`name=match`, `definition=http://hl7.org/fhir/OperationDefinition/Patient-match`) so DIBBs auto-detects `$match`.
- Keep the endpoint additive, destination-agnostic, and behind the existing mTLS/role authorization.

**Non-Goals:**
- No dynamic, per-destination capability reflection (same document for every `destinationId`; no destination lookup, no `404`).
- No enumeration of every derivable resource type listed in the controller javadoc (ServiceRequest, Organization, Practitioner, Location, etc.); only the independently queryable endpoints are advertised.
- No SMART-on-FHIR / OAuth security extensions, no `Patient/$match` request/response schema changes, and no change to any existing endpoint.
- No switch to a HAPI `RestfulServer` (the auto-generating alternative is out of scope).

## Decisions

### Decision 1: Build the CapabilityStatement with HAPI R4 model classes (not a hardcoded JSON string)
Construct an `org.hl7.fhir.r4.model.CapabilityStatement` programmatically and return it as `ResponseEntity<CapabilityStatement>` (or `ResponseEntity<Resource>`), letting the existing `FhirConverter` serialize it.
- **Rationale:** Consistent with every other handler in the controller; guarantees structurally valid FHIR; automatically participates in the existing content negotiation (`application/fhir+json`, `+xml`, etc.); no new dependency.
- **Alternatives considered:** (a) Return a hardcoded JSON string / static resource file — rejected: bypasses the FHIR converter and content negotiation, easy to drift into invalid FHIR, and duplicates media-type handling. (b) Introduce a HAPI `RestfulServer` to auto-generate `/metadata` — rejected: large architectural change, out of scope, and would collide with the existing hand-rolled routes.

### Decision 2: Static content, built per request by a small private helper
Populate the statement from constants (status `active`, `fhirVersion 4.0.1`, `format`, the three resources, the `match` operation). Build a fresh instance per request via a private `buildCapabilityStatement()` helper rather than caching a shared singleton.
- **Rationale:** The document is tiny and identical for all destinations; building per request avoids sharing a mutable, non-thread-safe HAPI resource across concurrent requests. Keeps the handler within Checkstyle method-length/complexity limits.
- **Alternatives considered:** Cache one shared `CapabilityStatement` instance — rejected because HAPI model objects are mutable and not designed for concurrent reuse; the micro-optimization isn't worth the thread-safety risk. Caching a pre-serialized JSON string is possible later if profiling ever shows a hotspot (it won't).

### Decision 3: Advertise `$match` at the Patient resource level
Add a `Patient` `CapabilityStatementRestResourceOperationComponent` with `name=match` and `definition=http://hl7.org/fhir/OperationDefinition/Patient-match`.
- **Rationale:** Matches the ticket's minimal shape and DIBBs' primary detection path (`rest[0].resource[type=Patient].operation[name=match]`). DIBBs also accepts a global `rest[0].operation[name=match]`, but resource-level is the more precise/idiomatic FHIR placement.
- **Alternatives considered:** Global `rest.operation` only — works for DIBBs but is less precise and wouldn't localize the operation to `Patient`. We may additionally add the global entry if a future client needs it (tracked as an open question), but it is not required.

### Decision 4: Mapping, media types, and authorization mirror the existing endpoints
Add `@GetMapping(value = "/{destinationId}/metadata", produces = { "application/fhir+json", "application/fhir+xml", "application/fhir+yaml", "application/json", "application/xml", "application/yaml", "text/xml" })`. Inherit the class-level `@RolesAllowed({XFORM_SENDING_SYSTEM, ADMIN})`; the constructor's `AccessControlRegistry.register(this)` already covers the controller so the new path is governed by the same mTLS/role policy.
- **Rationale:** Uniform content negotiation and security with sibling endpoints; DIBBs probes `/metadata` with its configured client certificate, so no auth exemption is needed.
- **Alternatives considered:** Making `/metadata` anonymous per FHIR convention — explicitly rejected by product decision for this service (keep parity with other FHIR endpoints).

### Decision 5: Extract repeated string literals into constants
Resource type names and interaction/operation codes reused when building the statement (e.g. `"Patient"` already exists as `PATIENT`; add constants for the `match` name, the OperationDefinition URL, `"4.0.1"`, and `application/fhir+json`) to satisfy Checkstyle `MultipleStringLiterals`.
- **Rationale:** Avoids build failure at the `validate` phase and centralizes the values the tests assert on.

## Risks / Trade-offs

- **Checkstyle `MultipleStringLiterals` / method length** → Extract shared literals into constants and keep `buildCapabilityStatement()` focused; run `mvn validate`/Checkstyle before pushing.
- **`FhirConverter` Content-Type behavior (IGDD-3164)** → The Patient `$match` flow depends on that fix, but `/metadata` only returns a resource; mitigation: the test asserts the response is a JSON `CapabilityStatement` with the required fields, and end-to-end `$match` verification is gated on IGDD-3164. Track the dependency rather than blocking this change.
- **Wrong/typo'd `$match` `definition` URL or operation name** → DIBBs silently keeps `supportsMatch=false`. Mitigation: assert the exact `name=match` and canonical `definition` string in a unit test.
- **Access-control path coverage** → If `AccessControlRegistry`/`AccessControlValve` needs the new path explicitly allow-listed for the role, an authorized probe could 403. Mitigation: verify with a `@SpringBootTest` that an authorized request returns `200` and mirror how sibling GET endpoints are registered.
- **Over/under-advertising resources** → Advertising resources that aren't truly queryable could mislead clients; the minimal set (Patient/Immunization/ImmunizationRecommendation) mirrors the actual GET endpoints, so this is low risk.

## Migration Plan

- Purely additive: one new GET handler plus a private builder and string constants in `FhirController`; no schema, config-model, or repository changes; no data migration.
- Deploy through the normal pipeline (Maven package → Docker image → ECS dev). No feature flag needed.
- Update `docs/fhir/fhir-api.md` (and `docs/fhir/index.md`) and add a `/metadata` request to the Postman/Newman collection used by CI.
- **Rollback:** revert the added handler/helper; because nothing else depends on `/metadata`, removal is safe and immediate.
- **Post-deploy enablement (config only, no code):** in DIBBs v1.2.0, re-save the xform server so the capability probe re-runs and sets `supportsMatch=true`, then enable patient matching.

## Open Questions

- Should the statement also include a global `rest.operation` named `match` in addition to the Patient resource-level operation, for maximum client compatibility? (Not required by the spec; can be added cheaply if a consumer needs it.)
- Should optional metadata be populated (`software.name`/`software.version`, `implementation.description`/`url`, `publisher`, `date`) for nicer human-readable output, or kept minimal for now?
- Are the non-JSON representations (`application/fhir+xml`, `+yaml`) actually exercised by any consumer, or is advertising them via `produces` sufficient?
