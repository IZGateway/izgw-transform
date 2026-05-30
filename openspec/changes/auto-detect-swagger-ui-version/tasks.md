## 1. Implementation

- [ ] 1.1 Add a new class `SwaggerUiVersionConfig` under `src/main/java/gov/cdc/izgateway/xform/configuration/` annotated with `@Configuration` and `@Slf4j` that constructor-injects `org.springdoc.core.properties.SwaggerUiConfigProperties`.
- [ ] 1.2 In `SwaggerUiVersionConfig`, add a `@PostConstruct` method that calls `new org.webjars.WebJarVersionLocator().version("swagger-ui")`, and when the returned value is non-blank, calls `setVersion(detected)` on the injected `SwaggerUiConfigProperties` bean.
- [ ] 1.3 On successful detection, emit exactly one `INFO` log line (e.g., `log.info("Detected swagger-ui webjar version: {}", detected);`) — satisfies the "Detected swagger-ui version is logged at startup" requirement.
- [ ] 1.4 On null/blank result or thrown `RuntimeException`, catch and emit one `WARN` log line including the cause and leave the version field untouched — satisfies the "Detection failure logs a warning" scenario; the application MUST still start.
- [ ] 1.5 Add a class-level Javadoc that briefly explains why the override exists (drift between BOM-managed `org.webjars:swagger-ui` and Springdoc's bundled default in `springdoc.config.properties`) and references this change name.
- [ ] 1.6 Verify the `springdoc.swagger-ui.version` key has been removed from `src/main/resources/application.yml` (already deleted on the working branch; the task here is to confirm and to NOT re-add a placeholder comment).
- [ ] 1.7 Confirm no new Maven dependency is needed — `org.webjars:webjars-locator-lite:1.1.3` is already transitive via `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.17`. Do NOT add it explicitly to `pom.xml`.

## 2. Tests

- [ ] 2.1 Add a JUnit 5 `@SpringBootTest` named `SwaggerUiVersionConfigTests` under `src/test/java/gov/cdc/izgateway/xform/configuration/` that loads the full Spring context and asserts the `SwaggerUiConfigProperties.version` field is non-null/non-blank, equals the version that `WebJarVersionLocator.version("swagger-ui")` returns at test time, and matches the format `X.Y.Z`.
- [ ] 2.2 In the same test class, add a test that asserts the value of `SwaggerUiConfigProperties.version` is NOT the legacy hardcoded `5.32.5` (regression guard) and is NOT Springdoc's bundled default `5.32.2` whenever the BOM-resolved webjar version differs.
- [ ] 2.3 Add a `@SpringBootTest` integration test that issues `GET /swagger/ui.html` against the embedded Tomcat (using `TestRestTemplate` with the test mTLS client cert under `target/`) and asserts the response is HTTP 200 (after redirects) — satisfies the "Swagger UI loads at the configured path" scenario.
- [ ] 2.4 Add a sibling test that issues `GET /swagger/swagger-ui/index.html` and asserts HTTP 200 with `Content-Type: text/html` and a body containing the literal string `<title>Swagger UI</title>` — satisfies the "Static asset paths resolve to the actual webjar directory" scenario.
- [ ] 2.5 Add a test that issues `GET /swagger/api-docs` and asserts HTTP 200 with `Content-Type: application/json` and a body parseable as JSON whose top-level key `openapi` is present — satisfies the "OpenAPI document is served" scenario.
- [ ] 2.6 Run the full unit-test suite via `mvn test` to confirm no regressions in unrelated tests (operations, preconditions, repository backends, AspectJ advice).

## 3. Quality gates

- [ ] 3.1 Run `mvn -pl . validate` (or `mvn clean package`) to confirm Checkstyle (`ai-checkstyle.xml`) passes — keep `SwaggerUiVersionConfig` under the cyclomatic-complexity and method-length thresholds.
- [ ] 3.2 Confirm the new class has no `MultipleStringLiterals` violation (use a `private static final String SWAGGER_UI_WEBJAR_NAME = "swagger-ui";` if the literal is referenced more than three times).
- [ ] 3.3 Run `mvn clean package` end-to-end (including OWASP dependency-check unless `-DskipDependencyCheck=true`) and confirm no new CVSS ≥ 7 findings are introduced.
- [ ] 3.4 Launch the fat-jar locally per `docs/QUICK_START.md` (`docker/data/lib/aspectjweaver-1.9.22.jar` + `spring-instrument-5.3.8.jar` javaagents, `SSL_SHARE=./target`, `COMMON_PASS=XFORM_TESTING_COMMON_PASS`) and verify in a browser that `https://localhost:444/swagger/ui.html` loads the Swagger UI end-to-end with the on-classpath swagger-ui version.
- [ ] 3.5 Confirm the startup log includes the `Detected swagger-ui webjar version: <version>` `INFO` line.

## 4. Documentation

- [ ] 4.1 In `docs/CONFIGURATION_REFERENCE.md`, remove (or mark as "auto-detected; do not set") the row documenting `springdoc.swagger-ui.version`.
- [ ] 4.2 Cross-check `docs/QUICK_START.md` for any reference to `springdoc.swagger-ui.version`; remove if present.
- [ ] 4.3 No CHANGELOG file exists at the repo root; if you add release notes elsewhere, mention "Swagger UI now tracks the on-classpath `org.webjars:swagger-ui` version automatically; manual `springdoc.swagger-ui.version` pinning is no longer required."

## 5. Security review

- [ ] 5.1 Confirm the change does NOT touch `AccessControlValve`, `AccessControlService.checkSwaggerAccess`, `Roles`, JWT validation, mTLS keystore handling, or BCFIPS provider setup. Mark the security review as "no change required" in the PR description.
- [ ] 5.2 No new env vars, no new SPRING_DATABASE values, no new HTTP endpoints — confirm and note this explicitly in the PR description so reviewers do not look for a deployment-side change.

## 6. Verification against specs

- [ ] 6.1 Manually walk through each scenario in `openspec/changes/auto-detect-swagger-ui-version/specs/api-documentation/spec.md` and confirm the implementation + tests cover it. Update the checklist if any scenario is partially covered.
- [ ] 6.2 Run `openspec verify --change auto-detect-swagger-ui-version` (or `/opsx:verify`) before archiving.
