## 1. Implementation

- [ ] 1.1 Add `import ca.uhn.fhir.rest.param.ReferenceParam;` to `FhirController` (alongside the existing `DateParam`/`TokenParam` imports).
- [ ] 1.2 Add a `private static final String SUBJECT = "subject";` constant near the existing `PATIENT = "Patient"` constant (keep field declarations together for `DeclarationOrder`).
- [ ] 1.3 Add `private static boolean isPatientReference(String value)` — returns true for a blank-guarded value whose `ReferenceParam.getResourceType()` is null/empty (bare id) or equals `PATIENT`; false otherwise. Reuse the `PATIENT` constant for the type comparison.
- [ ] 1.4 Add `private static HttpServletRequest normalizeSubjectToPatient(HttpServletRequest req)` — returns `req` unchanged when no `subject` parameter is present; otherwise wraps in `RequestWithModifiableParameters` (reusing an existing wrapper if `req` is already one), copies each `subject` value that passes `isPatientReference` to `patient` only when `patient` is absent (`Immunization.SP_PATIENT` for the param name), removes `subject`, and returns the wrapper.
- [ ] 1.5 Call `req = normalizeSubjectToPatient(req);` as the first statement of `FhirController.processQuery`, before `queryType`/parameter handling, with a brief comment that `subject` is a lenient alias for `patient`.

## 2. Tests

- [ ] 2.1 Add/extend a JUnit 5 `@SpringBootTest` FHIR controller test: `subject=Patient/<base64 system|value>` produces a QBP_Q11 whose QPD-3 is identical to `patient=Patient/<same>` (QPD-3.1 value, QPD-3.2.1 assigning authority, QPD-3.5 = `MR`). Use a known value such as base64 of `NV0000|3973565` (`TlYwMDAwfDM5NzM1NjU`).
- [ ] 2.2 Test the Patient-only guard: `subject=Group/<id>` (no other patient params) is dropped and yields the existing `400` validation message, not a decode error.
- [ ] 2.3 Test precedence: both `patient=Patient/<idA>` and `subject=Patient/<idB>` present → query built from `<idA>` only.
- [ ] 2.4 Test the `POST .../_search` form variant applies the same aliasing as GET.
- [ ] 2.5 Regression: an existing `patient=` / `patient.identifier` / `patient.*` request is unchanged by the new code path.
- [ ] 2.6 Run the relevant tests via Maven (surefire env/keystore setup), e.g. `mvn test -Dtest=<FhirControllerTest>`.

## 3. Documentation

- [ ] 3.1 Update the FHIR endpoint reference under `docs/fhir/` to note that `subject` is accepted as a non-standard alias for `patient` (Patient references / bare ids only), that `patient` takes precedence, and that senders should migrate to `patient=`.
- [ ] 3.2 Update the Postman/Newman collection under `testing/scripts/` to add a `subject=`-based query case (and confirm existing `patient=` cases still pass) so CI exercises the alias.

## 4. Verification

- [ ] 4.1 Confirm Checkstyle passes at the `validate` phase (no `MultipleStringLiterals`, `DeclarationOrder`, complexity, or method-length violations from the new code).
- [ ] 4.2 Confirm OWASP dependency-check stays under CVSS 7 (no new dependencies are introduced — `ReferenceParam` is already on the classpath via the existing HAPI FHIR dependency).
- [ ] 4.3 Build cleanly with `mvn clean package` and confirm the new tests run.

## 5. Notes

- [ ] 5.1 No new `Operation`/`Precondition` subclass is introduced, so no `@JsonSubTypes` registration is required.
- [ ] 5.2 No auth (mTLS/JWT/Roles) or crypto/BCFKS changes are involved; no separate security review is required beyond normal code review. The change only rewrites a request parameter name on an already-authenticated request.
