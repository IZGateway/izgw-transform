## ADDED Requirements

### Requirement: Swagger UI endpoint path

The transformation service SHALL expose Swagger UI for the `/api/v1/**` REST surface at the path `/swagger/ui.html` and SHALL expose the OpenAPI document at `/swagger/api-docs`. The service SHALL NOT require any springdoc-related properties beyond `springdoc.swagger-ui.path`, `springdoc.api-docs.path`, and `springdoc.paths-to-match` in `src/main/resources/application.yml`.

#### Scenario: Swagger UI loads at the configured path

- **GIVEN** the transformation service is running on `https://<host>:<port>` with the default `application.yml`
- **AND** the requester has the `ADMIN` role per `AccessControlService.checkSwaggerAccess`
- **WHEN** the requester issues `GET /swagger/ui.html`
- **THEN** the response is an HTTP 200 (or a 3xx redirect chain terminating at HTTP 200) and the body contains the Swagger UI HTML

#### Scenario: OpenAPI document is served

- **GIVEN** the transformation service is running with the default `application.yml`
- **AND** the requester has the `ADMIN` role
- **WHEN** the requester issues `GET /swagger/api-docs`
- **THEN** the response is HTTP 200 with a JSON body conforming to the OpenAPI 3 specification

#### Scenario: Configuration is minimal

- **GIVEN** the transformation service is running with the default `application.yml`
- **WHEN** any `springdoc.swagger-ui.version` property is present or absent in the active Spring environment
- **THEN** Swagger UI behavior MUST NOT depend on that property value (it MAY be absent entirely)

### Requirement: Swagger UI static resources resolve regardless of swagger-ui webjar version

The service SHALL successfully serve the Swagger UI static resources (`index.html`, `swagger-ui-bundle.js`, `swagger-initializer.js`, CSS, and fonts) at `/swagger/swagger-ui/**` for **whatever version of `org.webjars:swagger-ui` is on the application classpath at runtime**. The service MUST NOT require manual configuration changes (yaml, pom, or otherwise) when the `org.webjars:swagger-ui` version changes via izgw-bom updates.

#### Scenario: Swagger UI index loads after a webjar bump

- **GIVEN** the izgw-bom has bumped `org.webjars:swagger-ui` to a new version (e.g., from `5.32.2` to a later patch release)
- **AND** the transformation service has been rebuilt against the updated bom with no other changes to `src/main/resources/application.yml`
- **AND** the requester has the `ADMIN` role
- **WHEN** the requester issues `GET /swagger/swagger-ui/index.html`
- **THEN** the response is HTTP 200 and the body is the Swagger UI `index.html` for the on-classpath webjar version

#### Scenario: Static asset paths resolve to the actual webjar directory

- **GIVEN** `org.webjars:swagger-ui` version `X.Y.Z` is the version Maven resolves onto the classpath
- **WHEN** the application starts
- **THEN** Springdoc's swagger-ui static-resource handler MUST be configured to serve from `classpath:META-INF/resources/webjars/swagger-ui/X.Y.Z/` (the directory that actually exists in the resolved webjar)

### Requirement: Detected swagger-ui version is logged at startup

The service SHALL emit exactly one log entry at `INFO` level during application startup that reports the detected `org.webjars:swagger-ui` version.

#### Scenario: Successful detection logs the version

- **WHEN** the application starts and `org.webjars:swagger-ui` is on the classpath
- **THEN** a single `INFO` log message is written by a logger under `gov.cdc.izgateway.xform.*` containing the literal string `swagger-ui` and the detected version

#### Scenario: Detection failure logs a warning

- **WHEN** the application starts and the detection lookup returns `null` or throws an exception
- **THEN** a single `WARN` log message is written by a logger under `gov.cdc.izgateway.xform.*` describing the failure
- **AND** the application MUST continue to start (the failure MUST NOT prevent the Spring context from initializing)

### Requirement: Backward compatibility with admin access control

The change MUST preserve the existing `/swagger/**` access-control rule enforced by `gov.cdc.izgateway.xform.services.AccessControlService.checkSwaggerAccess` (admin-only access).

#### Scenario: Non-admin user receives forbidden response

- **GIVEN** the requester is authenticated but does NOT hold the `ADMIN` role
- **WHEN** the requester issues `GET /swagger/ui.html`, `GET /swagger/api-docs`, or `GET /swagger/swagger-ui/index.html`
- **THEN** the response is HTTP 403 (the version-detection change has not relaxed or altered this rule)

#### Scenario: Admin access continues to work

- **GIVEN** the requester is authenticated and holds the `ADMIN` role
- **WHEN** the requester issues any `GET /swagger/**` request
- **THEN** the response is the same status that would have been returned before this change for the same request

### Requirement: Configuration model and message paths are unaffected

The change MUST NOT alter the inbound (SOAP/HL7v2, FHIR), outbound (`izghub`, `iis`), or loopback (`x-loopback: true`) message paths, NOR the configuration model (Organization, Pipeline, Pipe, Solution, Operation, Precondition, Mapping, AccessControl, User, GroupRoleMapping). Both repository backends (`file` and `dynamodb`) MUST behave identically before and after this change.

#### Scenario: Existing organization configs continue to work

- **GIVEN** an organization transformation configuration that worked under the previous version of the service
- **WHEN** the same SOAP, FHIR, or loopback request is replayed against the upgraded service
- **THEN** the transformation output is byte-for-byte identical to the pre-change output

#### Scenario: Repository backends are unaffected

- **GIVEN** `SPRING_DATABASE` is set to `file`, `dynamodb`, or `migrate`
- **WHEN** the service starts
- **THEN** repository initialization succeeds and the configured entities load identically to the pre-change behavior

#### Scenario: Operation and Precondition discriminators are unchanged

- **WHEN** the application deserializes a configuration JSON containing any registered `method` discriminator (`copy`, `mapper`, `regex_replace`, `set`, `save_state`, `equals`, `not_equals`, `exists`, `not_exists`, `regex_match`, and their HL7 v2 variants)
- **THEN** deserialization succeeds and produces the same runtime type as before the change (no new `@JsonSubTypes` entries are required by this change)
