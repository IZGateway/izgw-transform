## 1. Implementation

- [x] 1.1 Add `import ca.uhn.fhir.rest.param.ReferenceParam;` to `FhirController` (alongside the existing `DateParam`/`TokenParam` imports).
- [x] 1.2 Add a `private static final String SUBJECT = "subject";` constant near the existing `PATIENT = "Patient"` constant (keep field declarations together for `DeclarationOrder`).
- [x] 1.3 Add `isPatientReference(String value)` — returns true for a blank-guarded value whose `ReferenceParam.getResourceType()` is null/empty (bare id) or equals `PATIENT`; false otherwise. Reuse the `PATIENT` constant for the type comparison. (Package-private static for testability.)
- [x] 1.4 Add `normalizeSubjectToPatient(HttpServletRequest req)` — returns `req` unchanged when no `subject` parameter is present; otherwise wraps in `RequestWithModifiableParameters` (reusing an existing wrapper if `req` is already one), copies each `subject` value that passes `isPatientReference` to `patient` only when `patient` is absent (`Immunization.SP_PATIENT` for the param name), removes `subject`, and returns the wrapper. (Package-private static for testability.)
- [x] 1.5 Call `req = normalizeSubjectToPatient(req);` as the first statement of `FhirController.processQuery`, before `queryType`/parameter handling, with a brief comment that `subject` is a lenient alias for `patient`.

## 2. Tests

- [x] 2.1 FHIR controller test: `subject=Patient/<base64 system|value>` produces a QBP_Q11 whose QPD-3 is identical to `patient=Patient/<same>` (QPD-3.1 value, QPD-3.4.1 assigning authority, QPD-3.5 = `MR`), asserted via Terser. Uses base64 of `NV0000|3973565` (`TlYwMDAwfDM5NzM1NjU`). (Plain unit test matching the existing `FhirControllerTests` style — no Spring context needed.)
- [x] 2.2 Test the Patient-only guard: `subject=Group/<id>` (no other patient params) is dropped and the downstream query throws the existing `IllegalArgumentException` validation (→ 400), not a decode error.
- [x] 2.3 Test precedence: both `patient=Patient/<idA>` and `subject=Patient/<idB>` present → `patient` retained, `subject` ignored.
- [x] 2.4 `POST .../_search` form variant applies the same aliasing as GET — covered at the shared chokepoint: both paths call `processQuery` → `normalizeSubjectToPatient`, which operates on the parsed parameter map regardless of HTTP method. Verified by the normalization unit tests (a full MockMvc POST would require a Spring context the existing suite avoids).
- [x] 2.5 Regression: a `patient=` request with no `subject` is returned untouched (same instance) by the new code path.
- [x] 2.6 Ran via Maven: `mvn test -Dtest=FhirControllerTests -DskipDependencyCheck=true` → 9 tests, 0 failures, 0 errors.

## 3. Documentation

- [x] 3.1 Updated `docs/fhir/fhir-api.md` Query Parameters section with a note that `subject` is accepted as a non-standard alias for `patient` (Patient references / bare ids only), that `Group/...` is ignored, that `patient` takes precedence, and that clients should send `patient`.
- [x] 3.2 Updated the Postman/Newman collection (`testing/scripts/TS_Integration_Test.postman_collection.json`): added `patient=` (07b) and `subject=` (07c) identifier "returns data" cases plus `patient=`/`subject=` "no data" cases (08b/08c), in both the cert (mTLS) and JWT integration-test sections (full parity). A host-gated pre-request script adds `patient.family`/`patient.given` only for `*.phiz-project.org` hosts (to clear those hubs' catch-and-kill, which keys off QPD-4); dev/localhost send the pure identifier. No-data cases use a fixed non-existent `btoa('MYEHR|000000000')`. Verified working by the user against Dev and Onboard.

## 4. Verification

- [x] 4.1 Checkstyle passed at the `validate` phase — the Maven run reached and completed the test phase (which is gated behind `validate`), so the new code has no `MultipleStringLiterals`/`DeclarationOrder`/complexity/method-length violations.
- [x] 4.2 No new dependencies introduced — `ReferenceParam` is already on the classpath via the existing HAPI FHIR dependency (confirmed by successful compile), so the OWASP dependency-check posture (CVSS < 7) is unchanged. Full scan not re-run (no dependency delta).
- [x] 4.3 Ran `mvn clean package -DskipDependencyCheck=true` → BUILD SUCCESS: 186 tests, 0 failures, 0 errors; fat-jar `target/xform-0.20.0-SNAPSHOT.jar` produced. (OWASP dependency-check skipped per 4.2 — no dependency delta.)

## 5. Notes

- [x] 5.1 No new `Operation`/`Precondition` subclass is introduced, so no `@JsonSubTypes` registration is required.
- [x] 5.2 No auth (mTLS/JWT/Roles) or crypto/BCFKS changes are involved; no separate security review is required beyond normal code review. The change only rewrites a request parameter name on an already-authenticated request.
