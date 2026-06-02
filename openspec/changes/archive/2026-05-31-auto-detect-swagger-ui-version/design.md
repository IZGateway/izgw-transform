## Context

The transformation service uses Springdoc (`springdoc-openapi-starter-webmvc-ui:2.8.17`) to expose Swagger UI at `/swagger/ui.html` and OpenAPI JSON at `/swagger/api-docs`. Springdoc builds the swagger-ui static resource handler from the value of `SwaggerUiConfigProperties.version`, which it reads in this precedence order:

1. The `springdoc.swagger-ui.version` property in `application.yml` / Spring environment.
2. A fallback baked into `springdoc.config.properties` inside `springdoc-openapi-starter-common-<springdoc.version>.jar`.

For Springdoc 2.8.17 the baked-in fallback is `5.32.2`. The izgw-bom (parent POM `gov.cdc.izgw:izgw-bom:1.9.0-SNAPSHOT`) overrides `org.webjars:swagger-ui` to a newer version (currently `5.32.6`) and a nightly GitHub Actions workflow in the BOM (`dependency-updates.yml`) bumps this property on every release with a newer version available. The webjar's static assets live under `META-INF/resources/webjars/swagger-ui/<actual-version>/`, so when Springdoc serves `/swagger/swagger-ui/index.html` it asks the Spring resource handler for `classpath:META-INF/resources/webjars/swagger-ui/5.32.2/index.html` — a path that does not exist in the `5.32.6` jar — and the request fails with `404 — No static resource index.html`.

The previous workaround was to pin `springdoc.swagger-ui.version: <value>` in `src/main/resources/application.yml` (introduced in PR #246). That works as long as someone re-edits the line every time the BOM bumps the webjar, which has not been happening reliably. The pin has already been removed from `application.yml` on the working branch in preparation for this change.

`webjars-locator-lite:1.1.3` is already on the classpath (transitive via `springdoc-openapi-starter-webmvc-ui`). Its `org.webjars.WebJarVersionLocator#version(String webJarName)` reads `META-INF/maven/org.webjars/<webJarName>/pom.properties` from the jar and returns the actual installed version — exactly the value Springdoc needs.

Stakeholders: xform developers (who want zero manual yaml edits on BOM bumps), CI (must continue to build and run Postman tests unmodified), and ops (must continue to serve Swagger UI to authenticated admin users at `/swagger/ui.html`).

## Goals / Non-Goals

**Goals:**
- The Swagger UI works at `/swagger/ui.html` regardless of which `org.webjars:swagger-ui` version the BOM ships, with no manual intervention on each BOM bump.
- No new dependencies, no BOM changes, no yaml configuration burden, no CI workflow changes.
- Behavior is deterministic and easy to debug: on startup the application logs the detected swagger-ui version once at `INFO`, and on failure it logs a clear `WARN` and falls through to whatever Springdoc would otherwise have done.
- Admin-only access to `/swagger/**` continues to be enforced by the existing `AccessControlService.checkSwaggerAccess`.

**Non-Goals:**
- Changing the OpenAPI spec output or the `/api/v1/**` controller annotations.
- Changing the Swagger UI URL paths (`/swagger/ui.html`, `/swagger/api-docs`, `/swagger/swagger-ui/index.html`) — only the version-detection mechanism.
- Modifying the izgw-bom or its nightly dependency-update workflow.
- Adding tests that boot the full Spring context just for this; existing `@SpringBootTest` coverage and a manual Swagger UI load via browser are the verification.
- Detecting versions of any other webjar.

## Decisions

### Decision 1: Detect the swagger-ui version at runtime, do not pin it in configuration.

**Choice:** Add a small `@Configuration` in `gov.cdc.izgateway.xform.configuration` that depends on Springdoc's `SwaggerUiConfigProperties` bean and overwrites its `version` field with the value returned by `new WebJarVersionLocator().version("swagger-ui")` during a startup callback.

**Alternatives considered:**
- *Pin in `application.yml`* — current approach, breaks on every BOM bump. Rejected.
- *Pin in transform `pom.xml`* (override `<swagger-ui.version>` to match Springdoc's bundled default `5.32.2`) — moves the pin but does not self-heal when Springdoc itself upgrades. Rejected.
- *Remove the override from izgw-bom + add `org.webjars:swagger-ui` to `automation-exclusions.txt`* — works, but requires a BOM PR + ongoing coordination, and gives up automatic CVE bumps for swagger-ui. Useful fallback if Option B has unforeseen problems but not the first choice.
- *Maven build-time filtering of `application.yml`* with `@swagger-ui.version@` placeholder — workable but requires non-default delimiters (since `${…}` conflicts with Spring placeholders) and leaks a Maven property name into yaml; IDE runs against unfiltered resources show the literal placeholder. Rejected for ergonomic reasons.

**Rationale:** Runtime detection treats the on-classpath webjar as the source of truth, which is the only value Springdoc's resource handler actually needs to be correct. It is contained entirely within xform (no BOM coordination), self-heals across BOM bumps and Springdoc upgrades alike, and uses only already-present transitive dependencies.

### Decision 2: Apply the override via a static-`@Bean` `BeanPostProcessor` targeting the `SwaggerUiConfigProperties` bean.

**Choice:** A `@Configuration` class exposes a `static @Bean BeanPostProcessor` whose `postProcessAfterInitialization` recognises `SwaggerUiConfigProperties`, calls a package-private helper that runs `WebJarVersionLocator.version("swagger-ui")`, and unconditionally `setVersion(...)`s the result on the bean. The detector is wrapped in a `Supplier<String>` so tests can drive the null / blank / throwing branches without mocking.

**Alternatives considered:**
- *`@PostConstruct` on an `@Configuration` class that constructor-injects `SwaggerUiConfigProperties`* — initial design choice; abandoned during implementation. **Reason:** `src/main/resources/application.yml` sets `spring.main.lazy-initialization: true`, which means an `@Configuration` class with no consumers is never instantiated and its `@PostConstruct` never fires. (Verified empirically — the `Detected swagger-ui webjar version: …` INFO line was missing from the `XformApplicationTests` boot log under this design.)
- *`@Lazy(false)` on the same `@Configuration` to force eager init* — would solve the activation problem but leaves the ordering question: any other bean that *consumes* `SwaggerUiConfigProperties.version` (Springdoc's resource-handler configurer) might run before the `@PostConstruct` does, depending on Spring's resolution order. Two beans both depending on `SwaggerUiConfigProperties` have no guaranteed init order.
- *`SmartInitializingSingleton`* — guaranteed to fire after all singletons are wired, but still after the resource-handler bean has read the version field. Same ordering problem.
- *`EnvironmentPostProcessor` injecting a property source `springdoc.swagger-ui.version=<detected>`* — would work but runs very early (before logging is fully configured), couples to Spring Boot's property-loading internals, and gains nothing over the BPP approach.
- *Custom `SwaggerUiConfigProperties` subclass / `@Primary` bean override* — fragile across Springdoc upgrades because Springdoc autoconfigures the bean conditionally and may rename it.

**Rationale:** The BPP approach wins on two fronts at once. (1) BPPs are always eagerly registered and invoked, even when global lazy-init is on, so activation is guaranteed. (2) Mutating the bean inside its own `postProcessAfterInitialization` happens *before any other consumer can read it*, so the resource-handler configurer is guaranteed to see the corrected version. The two reasons compose: the BPP solves both the activation problem (#1) *and* the ordering problem (#2) that a `@PostConstruct` / `@Lazy(false)` combo only solves halfway. The static `@Bean` declaration is the documented pattern for registering `BeanPostProcessor`s without losing them to the `@Configuration` enhancer.

### Decision 3: Fail soft, not hard.

**Choice:** If `WebJarVersionLocator.version("swagger-ui")` returns `null` (no swagger-ui webjar on classpath) or throws, log a single `WARN` with the cause and leave `SwaggerUiConfigProperties.version` untouched. The application MUST continue to start.

**Rationale:** The Swagger UI is a developer-facing convenience, not a runtime business path. A misconfiguration that prevents Swagger UI from loading must not block message transformation. The existing Springdoc default (currently `5.32.2`) will continue to be used in the degenerate case, matching pre-change behavior.

### Decision 4: Log the detected version at startup.

**Choice:** Emit one `INFO` log line at startup with the detected version, e.g. `Detected swagger-ui webjar version: 5.32.6`. No log at every request; no metrics; no health indicator.

**Rationale:** Cheap diagnostic for anyone hitting a future 404. The `gov.cdc.izgateway.xform` logger root is already at `INFO` per `application.yml`, so this surfaces without log-level changes.

### Decision 5: Remove the now-stale yaml line, do not leave a "documenting" comment.

**Choice:** Delete the `springdoc.swagger-ui.version` key entirely from `src/main/resources/application.yml`. Do not leave a commented-out version or a "see SwaggerUiVersionConfig.java" comment in the yaml.

**Rationale:** Per project convention, comments that explain the absence of code rot quickly; the `@Configuration` class will carry one Javadoc line referencing this change.

## Risks / Trade-offs

- **Risk: Springdoc 3.x renames `SwaggerUiConfigProperties` or changes how it consumes the version.** → Mitigation: the override class is ~30 lines, easy to update during the Springdoc major-version bump. The Springdoc upgrade itself is the gating step for any breaking API change, so this will be caught and fixed alongside.
- **Risk: `WebJarVersionLocator` lookup fails silently and Springdoc falls back to its bundled default (`5.32.2`).** → Mitigation: Decision 3 + a `WARN` log line make the failure visible. Behavior degrades to the pre-change state, which is no worse than today's status quo. The package-private `Supplier`-based overload (`alignVersion(props, detector)`) lets unit tests directly exercise the null / blank / throwing branches.
- **Risk: Multiple swagger-ui webjars on the classpath (e.g., a duplicate from a transitive somewhere).** → Mitigation: in practice the izgw-bom narrows this to a single version. `WebJarVersionLocator` returns the version it finds first; if it picks the "wrong" one, the symptom is the same as today (404), and the `WARN` line would surface during diagnosis. Acceptable.
- **Risk: A BPP that doesn't match the targeted bean type is silently a no-op across Springdoc upgrades.** → Mitigation: the `SwaggerUiVersionContextTests` `@SpringBootTest` asserts `SwaggerUiConfigProperties.version` matches the on-classpath webjar version in the real container — any future regression that breaks bean matching (rename, conditional removal) fails that test.
- **Trade-off: One extra Java file the team has to maintain.** Acceptable cost compared to a recurring manual yaml-edit chore on every BOM bump.
- **Trade-off: The fix is xform-local and does not benefit other izgw consumers of the BOM.** Other services with the same drift would each need to adopt the same pattern. Acceptable — when there's a second consumer the right move is to lift this into izgw-core, not pre-emptively.

## Migration Plan

1. Land this change on `develop`. No deployment coordination needed.
2. Rebuild the fat-jar locally and verify `/swagger/ui.html` loads against a fresh local build.
3. Standard CI (PR build, OWASP dependency-check, Newman/Postman tests on dev cluster) is enough; no extra rollout steps.
4. Roll back, if needed, by reverting the commit — the `application.yml` line can be re-added in the revert if Swagger UI breakage reappears.

## Open Questions

None blocking. (`docs/CONFIGURATION_REFERENCE.md` currently lists `springdoc.swagger-ui.version` as a configurable property; the doc update to remove that row is part of `tasks.md` and not a blocker.)
