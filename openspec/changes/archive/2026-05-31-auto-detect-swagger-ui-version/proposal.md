## Why

Springdoc 2.8.17 reads its swagger-ui resource-handler version from `springdoc.swagger-ui.version` (with a fallback baked into `springdoc.config.properties` inside the springdoc starter jar — currently `5.32.2`). The izgw-bom overrides `org.webjars:swagger-ui` to a newer version (currently `5.32.6`) and the nightly auto-dependency job keeps bumping it. The two values drift, the resource-handler points at a webjar directory that does not exist, and `/swagger/swagger-ui/index.html` returns `404 — No static resource index.html`. Today's workaround is a manual `springdoc.swagger-ui.version: <pinned>` line in `src/main/resources/application.yml` that has to be edited every time the BOM bumps swagger-ui — easy to miss and currently broken.

## What Changes

- Add a small Spring `@Configuration` in `gov.cdc.izgateway.xform.configuration` that detects the actual `swagger-ui` webjar version at application startup using `org.webjars.WebJarVersionLocator` (already on the classpath via `webjars-locator-lite:1.1.3`, transitively pulled in by `springdoc-openapi-starter-webmvc-ui`) and force-sets it on Springdoc's `SwaggerUiConfigProperties` bean.
- Remove the manual `springdoc.swagger-ui.version` pin from `src/main/resources/application.yml` (the line is already deleted on the working branch; this change formalizes that deletion).
- No change to the swagger-ui URL paths (`/swagger/ui.html`, `/swagger/api-docs`, `/swagger/swagger-ui/index.html`) — the only thing changing is *how* Springdoc learns which webjar version to serve from.

## Capabilities

### New Capabilities

- `api-documentation`: Behavior of the Swagger UI / OpenAPI endpoints that document the xform `/api/v1/**` REST surface — the path, the version-detection mechanism that keeps the UI working across dependency bumps, and the existing access-control behavior (admin-only).

### Modified Capabilities

(None — no existing spec covers the Swagger UI today.)

## Impact

- **Code**: Adds one new `@Configuration` class (~15–25 lines + Lombok/Javadoc) under `gov.cdc.izgateway.xform.configuration`. Deletes one line from `src/main/resources/application.yml`. No changes to controllers, Camel routes, repository backends, operations, preconditions, or security.
- **Dependencies**: No new dependencies. Uses `org.webjars:WebJarVersionLocator` from `webjars-locator-lite:1.1.3` (already transitive) and `org.springdoc.core.properties.SwaggerUiConfigProperties` (already transitive).
- **Inbound/outbound paths**: SOAP/HL7v2 (Hub, IIS) and FHIR REST paths are unaffected. Only the developer-facing `/swagger/**` endpoints are touched.
- **Config model**: `Organization`, `Pipeline`, `Solution`, `Operation`, `Precondition`, `Mapping`, `AccessControl`, `User`, `GroupRoleMapping` — all unaffected.
- **Repository backends**: Both `file` and `dynamodb` repositories are unaffected; no `SPRING_DATABASE=migrate` implications.
- **Downstream consumers**: izghub / iis SOAP callers and Hub/IIS message consumers are unaffected — no wire-format or transformation behavior changes.
- **Backward compatibility**: Existing organization transformation configurations work unchanged. The admin-only access rule for `/swagger/**` enforced by `AccessControlService.checkSwaggerAccess` is preserved.
- **Docs**: `docs/CONFIGURATION_REFERENCE.md` lists `springdoc.swagger-ui.version` — that row should be removed (or marked auto-detected) once the change lands. `docs/QUICK_START.md` and `docs/APPLICATION_CONFIGURATION_STORAGE.md` do not need updates.
- **CI**: No changes to `.github/workflows/maven.yml`; no new env vars; OWASP dependency-check is unaffected (still gates on CVSS ≥ 7 on actual on-classpath jars).
