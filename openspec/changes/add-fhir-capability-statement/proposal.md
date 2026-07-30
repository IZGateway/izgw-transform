## Why

FHIR R4 requires that a server SHALL publish a `CapabilityStatement` via the capabilities interaction at `[base]/metadata` (https://hl7.org/fhir/R4/http.html#capabilities), but xform's hand-rolled FHIR controller currently serves no `/metadata` at all. This blocks the eHealth Exchange / DIBBs immunization pilot: DIBBs Query Connector only enables the FHIR `Patient/$match` operation when it **auto-detects** support by fetching `/metadata` and finding a `Patient` operation named `match`. Because xform returns nothing there, DIBBs sets `supportsMatch = false` and never uses `$match`, so the "candidate list" / "no certain match" flows (IGDD-3171) are unreachable in stock DIBBs. Serving a conformant CapabilityStatement is both a FHIR requirement and the switch that turns on `$match`-based matching for the pilot.

## What Changes

- Add a new read-only endpoint `GET /fhir/{destinationId}/metadata` on `FhirController` (`gov.cdc.izgateway.xform.endpoints.fhir`) that returns a FHIR R4 `CapabilityStatement`.
- The response is `200` with content type `application/fhir+json` (honoring existing FHIR content negotiation for xml/yaml where applicable) and declares `fhirVersion: "4.0.1"` and `format: ["application/fhir+json"]`.
- The CapabilityStatement advertises the FHIR resources xform actually exposes as queryable endpoints, with the `search-type` interaction: `Patient`, `Immunization`, `ImmunizationRecommendation`.
- It advertises the **Patient `$match`** operation as a `Patient` resource-level `operation` named `match` with `definition = http://hl7.org/fhir/OperationDefinition/Patient-match`, which is what DIBBs looks for.
- The endpoint returns the **same** CapabilityStatement for any `{destinationId}` value — no destination lookup, no `404`/`OperationOutcome` for unknown destinations (mirrors the fixed conformance document contract).
- The endpoint keeps the controller's existing security posture: mTLS/role authorization (`@RolesAllowed({XFORM_SENDING_SYSTEM, ADMIN})`); DIBBs probes `/metadata` with its configured client certificate.
- Purely additive: no existing FHIR endpoint behavior, request/response contract, or status code changes.

## Capabilities

### New Capabilities
- `fhir-capability-statement`: The `GET /fhir/{destinationId}/metadata` conformance endpoint — what it returns, the required `CapabilityStatement` shape (status, `fhirVersion`, `format`, resources/interactions, the Patient `$match` operation), content negotiation, authorization, and destinationId handling.

### Modified Capabilities
<!-- No requirement changes to existing specs. Existing specs (fhir-immunization-query,
     api-documentation) describe search/$match query behavior and API docs, which are
     unchanged by adding a separate, additive conformance endpoint. -->
- None.

## Impact

- **Inbound path affected:** FHIR REST only (new `GET /fhir/{destinationId}/metadata`). SOAP/HL7v2 inbound is unaffected.
- **Outbound paths (izghub, iis):** None. The endpoint returns a static conformance document and does not call `HubController`, the Camel `XformRouter` route, `DataXformService`, or any IIS/Hub backend, so it makes no outbound requests.
- **Config model:** No changes to Organization / Pipeline / Solution / Operation / Precondition, and no new `@JsonSubTypes` registrations. No transformation configuration is read or written.
- **Repository backends:** No impact on either the file or DynamoDB backend; no `SPRING_DATABASE=migrate` implications, since no persisted config is involved.
- **Downstream Hub/IIS consumers & backward compatibility:** No backward-compatibility risk. Existing FHIR search / `$match` endpoints and all SOAP/Hub contracts are untouched; this only adds a new GET route.
- **Code:** `src/main/java/gov/cdc/izgateway/xform/endpoints/fhir/FhirController.java` (new handler method; likely a small helper to build the `CapabilityStatement` using the HAPI `org.hl7.fhir.r4.model.CapabilityStatement` classes already on the classpath). New JUnit 5 `@SpringBootTest` coverage in `FhirControllerTests`.
- **Dependencies:** None added — HAPI FHIR R4 model classes are already used throughout `FhirController`.
- **Docs:** Update `docs/fhir/fhir-api.md` (and `docs/fhir/index.md`) to document the new `/metadata` endpoint; note it in the Postman/Newman collection used by CI.
- **Related:** IGDD-3171 (`$match` TM handling) becomes reachable in stock DIBBs once this ships; IGDD-3164 (FhirConverter Content-Type bug) is a prerequisite for `$match` to actually work end-to-end.
