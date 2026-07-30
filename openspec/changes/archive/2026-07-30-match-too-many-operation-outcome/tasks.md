## 1. Core Implementation

- [x] 1.1 Add constants to `FhirController` (or the detection helper) for the v2-0208 code system URL, the `TM` code, and the full "did not find a certain match" response text (satisfies Checkstyle `MultipleStringLiterals`)
- [x] 1.2 Implement a package-private static detection helper: given the converted Bundle, return the matched TM query-status issue (or empty) when the Bundle has zero Patient entries AND an OperationOutcome issue carries a `details.coding` with system `http://terminology.hl7.org/CodeSystem/v2-0208` and code `TM` (case-insensitive); null-safe over bundle/entries/issues/codings, fail-open to the Bundle path
- [x] 1.3 Implement a helper that builds the top-level `OperationOutcome` response: `severity` = `warning`, `code` = `multiple-matches` (`IssueType.MULTIPLEMATCHES`), `details.text` = "The matching operation found one or more possible matches, but did not find a certain match.", `details.coding` carried over from the detected source issue
- [x] 1.4 Branch in `iisPatientMatch` immediately after `processQuery(...)`: when detection triggers, return the OperationOutcome with HTTP 422 and `ContentUtils.getHeaders(req)`; otherwise proceed unchanged through `IDIMatch.score` and the 200 Bundle return
- [x] 1.5 Update the `iisPatientMatch` Swagger annotations to document the 422 response (top-level OperationOutcome, too-many-matches case)

## 2. Unit Tests (JUnit 5 via Maven)

- [x] 2.1 Add tests to `FhirControllerTests` (or a focused test class) for the detection helper: triggers on zero Patients + TM coding; does NOT trigger when a Patient entry is present alongside TM; does NOT trigger for `NF`/`OK`/`AE`/`AR` codings, empty bundles without TM, null/missing details or codings; matches `tm` case-insensitively
- [x] 2.2 Add tests for the response builder: resourceType `OperationOutcome`, warning severity, `multiple-matches` code, `details.text` contains the exact substring `did not find a certain match`, source TM coding (including display) preserved
- [x] 2.3 Add a test covering the full reshape decision at the `iisPatientMatch` level (fabricated converted Bundle → 422 OperationOutcome vs. scored 200 Bundle), running via `mvn test -Dtest=FhirControllerTests` (surefire env/keystore setup required)

## 3. Integration Tests (Postman/Newman)

- [x] 3.1 Add a `$match` "too many matches" request to `testing/scripts/TS_Integration_Test.postman_collection.json` using demographics known to produce a Z33/TM response from the dev IIS; assert top-level `resourceType` = `OperationOutcome`, `issue[].details.text` contains `did not find a certain match`, `issue[].details.coding` has the v2-0208 `TM` code, and HTTP status 422
- [x] 3.2 Verify the existing `$match` found/not-found Postman assertions still pass unchanged (candidate list → 200 searchset Bundle; `NF` no-match → 200 empty Bundle), for both JSON and XML variants
- [x] 3.3 Verify against DIBBs Query Connector v1.2.0 in the pilot environment: too-many → "No Certain Match Found"; within-limit candidates → pick-list (record outcome in the PR)

## 4. Docs and Verification

- [x] 4.1 Document the `$match` 422/OperationOutcome behavior (response shape, trigger condition, rationale) in the relevant docs — `docs/` FHIR/endpoint documentation if present, otherwise the `FhirController` Javadoc — and note the behavior change for release notes
- [x] 4.2 Run `mvn clean package` and confirm Checkstyle passes at the `validate` phase and OWASP dependency-check stays under CVSS 7 (no new dependencies expected)
- [x] 4.3 Confirm no security-review surface: no auth (mTLS/JWT/Roles), crypto, or config-model changes in the diff
