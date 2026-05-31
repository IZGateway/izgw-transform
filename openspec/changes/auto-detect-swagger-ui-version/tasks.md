## 1. Implementation

- [x] 1.1 Add a new class `SwaggerUiVersionConfig` under `src/main/java/gov/cdc/izgateway/xform/configuration/` annotated with `@Configuration` and `@Slf4j` that constructor-injects `org.springdoc.core.properties.SwaggerUiConfigProperties`.
- [x] 1.2 In `SwaggerUiVersionConfig`, add a `@PostConstruct` method that calls `new org.webjars.WebJarVersionLocator().version("swagger-ui")`, and when the returned value is non-blank, calls `setVersion(detected)` on the injected `SwaggerUiConfigProperties` bean.
- [x] 1.3 On successful detection, emit exactly one `INFO` log line (e.g., `log.info("Detected swagger-ui webjar version: {}", detected);`) — satisfies the "Detected swagger-ui version is logged at startup" requirement.
- [x] 1.4 On null/blank result or thrown `RuntimeException`, catch and emit one `WARN` log line including the cause and leave the version field untouched — satisfies the "Detection failure logs a warning" scenario; the application MUST still start.
- [x] 1.5 Add a class-level Javadoc that briefly explains why the override exists (drift between BOM-managed `org.webjars:swagger-ui` and Springdoc's bundled default in `springdoc.config.properties`) and references this change name.
- [x] 1.6 Verify the `springdoc.swagger-ui.version` key has been removed from `src/main/resources/application.yml` (already deleted on the working branch; the task here is to confirm and to NOT re-add a placeholder comment).
- [x] 1.7 Confirm no new Maven dependency is needed — `org.webjars:webjars-locator-lite:1.1.3` is already transitive via `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.17`. Do NOT add it explicitly to `pom.xml`.

## 2. Tests

- [x] 2.1 Add a JUnit 5 test class `SwaggerUiVersionConfigTests` under `src/test/java/gov/cdc/izgateway/xform/configuration/` that exercises the `@PostConstruct` directly against a fresh `SwaggerUiConfigProperties` instance and asserts the resulting version equals `WebJarVersionLocator.version("swagger-ui")` and matches `X.Y.Z`. (Implemented as a plain unit test rather than `@SpringBootTest` — see session notes.)
- [x] 2.2 In the same test class, add a test that asserts the resolved value is NOT the legacy hardcoded `5.32.5` (regression guard) and is NOT Springdoc's bundled default `5.32.2` when the BOM-resolved webjar version differs.
- [~] 2.3 ~~Add a `@SpringBootTest` integration test that issues `GET /swagger/ui.html`~~ — **Dropped.** No existing mTLS HTTP test plumbing in `src/test/java/`; standing it up would be 50–100+ lines disproportionate to the ~25-line production change. End-to-end Swagger UI is covered by manual task 3.4 against the local fat-jar.
- [~] 2.4 ~~Add a sibling test that issues `GET /swagger/swagger-ui/index.html`~~ — **Dropped.** Same reason as 2.3.
- [~] 2.5 ~~Add a test that issues `GET /swagger/api-docs`~~ — **Dropped.** Same reason as 2.3.
- [x] 2.6 Run the full unit-test suite via `mvn test` to confirm no regressions in unrelated tests (operations, preconditions, repository backends, AspectJ advice). **Result:** 174 tests pass.

## 3. Quality gates

- [x] 3.1 Run `mvn -pl . validate` (or `mvn clean package`) to confirm Checkstyle (`ai-checkstyle.xml`) passes — keep `SwaggerUiVersionConfig` under the cyclomatic-complexity and method-length thresholds. **Result:** 0 violations.
- [x] 3.2 Confirm the new class has no `MultipleStringLiterals` violation. **Done** — extracted `SWAGGER_UI_WEBJAR_NAME` constant; Checkstyle confirms.
- [x] 3.3 Run `mvn clean package` end-to-end (OWASP skipped via `-DskipDependencyCheck=true`; CI will re-run the full scan). **Result:** BUILD SUCCESS, repackaged fat-jar at `target/xform-0.19.0.jar`.
- [x] 3.4 Launch the fat-jar locally per `docs/QUICK_START.md` (`docker/data/lib/aspectjweaver-1.9.22.jar` + `spring-instrument-5.3.8.jar` javaagents, `SSL_SHARE=./target`, `COMMON_PASS=XFORM_TESTING_COMMON_PASS`) and verify in a browser that `https://localhost:444/swagger/ui.html` loads the Swagger UI end-to-end with the on-classpath swagger-ui version. **Verified by user — Swagger UI loaded without issue.**
- [x] 3.5 Confirm the startup log includes the `Detected swagger-ui webjar version: <version>` `INFO` line. **Verified** in the `XformApplicationTests` boot log: `Detected swagger-ui webjar version: 5.32.6` from `gov.cdc.izgateway.xform.configuration.SwaggerUiVersionConfig`.

## 4. Documentation

- [x] 4.1 In `docs/CONFIGURATION_REFERENCE.md`, remove (or mark as "auto-detected; do not set") the row documenting `springdoc.swagger-ui.version`. **No-op** — `grep -i swagger docs/CONFIGURATION_REFERENCE.md` returned no matches; the property was never documented there.
- [x] 4.2 Cross-check `docs/QUICK_START.md` for any reference to `springdoc.swagger-ui.version`; remove if present. **No-op** — no matches.
- [x] 4.3 No CHANGELOG file exists at the repo root. **No-op for this PR** — release notes can mention "Swagger UI now tracks the on-classpath `org.webjars:swagger-ui` version automatically" when next generated.

## 5. Security review

- [x] 5.1 Confirm the change does NOT touch `AccessControlValve`, `AccessControlService.checkSwaggerAccess`, `Roles`, JWT validation, mTLS keystore handling, or BCFIPS provider setup. Mark the security review as "no change required" in the PR description.
- [x] 5.2 No new env vars, no new SPRING_DATABASE values, no new HTTP endpoints — confirm and note this explicitly in the PR description so reviewers do not look for a deployment-side change.

## 6. Verification against specs

- [x] 6.1 Walked through each scenario in `specs/api-documentation/spec.md`:
  - *Swagger UI endpoint path* / *OpenAPI document is served* — covered by manual task 3.4 against the local fat-jar.
  - *Configuration is minimal* — `application.yml` has no `springdoc.swagger-ui.version` line.
  - *Swagger UI index loads after a webjar bump* / *Static asset paths resolve to the actual webjar directory* — covered by unit tests asserting the resolved version matches `WebJarVersionLocator.version("swagger-ui")`, plus manual task 3.4 end-to-end.
  - *Detected swagger-ui version is logged at startup* / *Detection failure logs a warning* — INFO branch confirmed in `XformApplicationTests` boot log (task 3.5); WARN branches are inline in the BPP and covered by code inspection.
  - *Backward compatibility with admin access control* / *Configuration model and message paths are unaffected* — the change adds one class touching only `SwaggerUiConfigProperties.version`; the 174-test regression run confirms no other behavior changed.
- [x] 6.2 Run `openspec validate auto-detect-swagger-ui-version`. **Result:** "Change 'auto-detect-swagger-ui-version' is valid".
