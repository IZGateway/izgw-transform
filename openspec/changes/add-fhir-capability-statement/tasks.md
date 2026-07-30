## 1. Implementation

- [ ] 1.1 In `FhirController` (`gov.cdc.izgateway.xform.endpoints.fhir`), add private constants for the values reused when building the statement — `match` operation name, the `$match` definition URL (`http://hl7.org/fhir/OperationDefinition/Patient-match`), `fhirVersion` (`4.0.1`), the `application/fhir+json` format, and the resource type names (`Immunization`, `ImmunizationRecommendation`; reuse the existing `PATIENT` constant) — to satisfy Checkstyle `MultipleStringLiterals`.
- [ ] 1.2 Add a private helper `buildCapabilityStatement()` that constructs an `org.hl7.fhir.r4.model.CapabilityStatement` with `status = active`, `fhirVersion = 4.0.1`, `format = [application/fhir+json]`, and a single `rest` entry with `mode = server`.
- [ ] 1.3 In the helper, add the `Patient`, `Immunization`, and `ImmunizationRecommendation` resources, each with a `search-type` interaction.
- [ ] 1.4 In the helper, add the `Patient` resource-level `operation` with `name = match` and `definition = http://hl7.org/fhir/OperationDefinition/Patient-match`.
- [ ] 1.5 Add the handler `@GetMapping(value = "/{destinationId}/metadata", produces = { "application/fhir+json", "application/fhir+xml", "application/fhir+yaml", "application/json", "application/xml", "application/yaml", "text/xml" })` returning `ResponseEntity<CapabilityStatement>` (or `ResponseEntity<Resource>`) with HTTP `200`; it takes `@PathVariable String destinationId` but performs no destination lookup and returns the same document for every value.
- [ ] 1.6 Add Javadoc and OpenAPI `@Operation`/`@ApiResponse` annotations consistent with the other handlers (200 = CapabilityStatement); keep the handler within Checkstyle method-length/complexity limits (delegate construction to `buildCapabilityStatement()`).
- [ ] 1.7 Confirm no `@JsonSubTypes` (Operation/Precondition) registration is required — this change adds no new Operation/Precondition subclass and touches no config model.

## 2. Tests

- [ ] 2.1 In `FhirControllerTests`, add a unit test that instantiates `FhirController` (mock `HubController`, `new FhirConfiguration()`, mock `AccessControlRegistry`, matching the existing tests), invokes the new `/metadata` handler, and asserts HTTP `200` and that the body is a `CapabilityStatement` with `status = active`.
- [ ] 2.2 Assert the CapabilityStatement declares `fhirVersion = 4.0.1` and `format` contains `application/fhir+json`.
- [ ] 2.3 Assert `rest[0].mode = server` and that `Patient`, `Immunization`, and `ImmunizationRecommendation` resources are each present with a `search-type` interaction.
- [ ] 2.4 Assert the `Patient` resource has an `operation` with `name = match` and `definition = http://hl7.org/fhir/OperationDefinition/Patient-match` (exact-string assertions so a typo can't silently break DIBBs detection).
- [ ] 2.5 Assert the same document is returned for two different `destinationId` values (destination-agnostic) and that an unknown/unconfigured `destinationId` still returns `200` (no `404`/`OperationOutcome`).
- [ ] 2.6 Add a mapping-annotation test (mirroring `postWithoutSearchIsExplicitlyMapped`) verifying the handler is mapped to `/{destinationId}/metadata` for `GET` with the expected `produces` media types.
- [ ] 2.7 Run the test class via Maven (`./mvnw -Dtest=FhirControllerTests test`) so surefire performs the env/BCFKS keystore setup; confirm the new tests pass.

## 3. Documentation & CI Collection

- [ ] 3.1 Document `GET /fhir/{destinationId}/metadata` (request, `200` `application/fhir+json` response, sample CapabilityStatement, auth requirement) in `docs/fhir/fhir-api.md` and link it from `docs/fhir/index.md`.
- [ ] 3.2 Add a `/metadata` request to the Postman/Newman collection used by the CI Newman step, asserting `200`, `fhirVersion = 4.0.1`, and presence of the Patient `match` operation.
- [ ] 3.3 Add a `RELEASE_NOTES.md` entry noting the new additive `/metadata` CapabilityStatement endpoint (referencing IGDD-3172).

## 4. Verification & Review

- [ ] 4.1 Run Checkstyle (`./mvnw validate`) and confirm the build passes (no `MultipleStringLiterals`, method-length, or complexity violations).
- [ ] 4.2 Run the full module build (`./mvnw -DskipITs=false verify` or the standard `./mvnw package`) and confirm OWASP dependency-check stays under CVSS 7 (no new dependencies were added, so this should be unaffected).
- [ ] 4.3 Security review note: confirm the endpoint inherits the class-level `@RolesAllowed({XFORM_SENDING_SYSTEM, ADMIN})` and is covered by `AccessControlRegistry`/`AccessControlValve` (mTLS) exactly like sibling FHIR endpoints — no new anonymous surface is introduced.
- [ ] 4.4 End-to-end (post-merge, environment-dependent, tracked with IGDD-3164): re-save the xform server in DIBBs v1.2.0, confirm `supportsMatch` is detected true and "Enable patient matching" appears, and that a `$match` search routes over `/Patient/$match`.
