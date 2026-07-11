# izgw-transform — Copilot Instructions

IZ Gateway Transformation Service. Spring Boot backend (Java 21) that intercepts HL7 V2
messages flowing between the IZ Gateway Hub and IIS endpoints, applies configurable
transformation pipelines, and exposes a REST management API consumed by the
`izg-transformation-ui` Xform Console.

**Skills:** `java-maven-style`, `xml-xslt-patterns` (inject before writing Java/Maven/XSLT)

**Public repo** — never include names of non-IZ-Gateway projects or organizations in commits.

---

## Build & Test

```cmd
mvn clean package          # compile, unit tests, build JAR and Docker image
mvn test                   # unit tests only
mvn dependency-check:check # OWASP CVE scan (slow; skipped in CI, run manually)
mvn clean site             # full site including JaCoCo, Checkstyle, Javadoc
```

Run a single test class:
```cmd
mvn test -Dtest=Hl7V2CopyTests
```

Run a single test method:
```cmd
mvn test -Dtest=Hl7V2CopyTests#testCopySegmentField
```

Test classes use the suffix `Tests` (not `Test`). Integration/functional tests run
post-deploy via Newman (Postman) — see `testing/scripts/`.

Checkstyle config: `ai-checkstyle.xml` / `ai-checkstyle-suppressions.xml`.

---

## Architecture

### Request Flow

```
Hub / IIS → HubController / IISController
              → Apache Camel (XformRouter)
                → DataXformService
                  → PipelineRunnerService          ← matches Pipeline by org + endpoints
                    → for each Pipe (if preconditions pass)
                        → Solution.execute()       ← applies ordered Operations
                → [forward to upstream Hub/IIS via Camel producer]
```

Camel routes `direct:izghubTransformerPipeline` and `direct:iisTransformerPipeline`
are the entry points. Two hosting modes: IZGHub-hosted (proxy) and IIS-hosted (standalone).

### Core Domain Concepts

| Concept | Class | Role |
|---|---|---|
| **Pipeline** | `model/Pipeline` | Binds an org + inbound/outbound endpoint pair to an ordered list of Pipes |
| **Pipe** | `model/Pipe` | References a Solution version + per-pipe Preconditions |
| **Solution** | `solutions/Solution` (runtime) / `model/Solution` (stored) | Ordered list of Operations applied to request and response |
| **Operation** | `operations/Operation` | Single transformation step (copy, set, regex_replace, mapper, save_state) |
| **Precondition** | `preconditions/Precondition` | Boolean gate before a Pipe executes (equals, exists, regex_match, …) |
| **ServiceContext** | `context/ServiceContext` | Carries the HL7 V2 message, direction, org, endpoints, and key-value state across all operations in a pipeline run |

Pipes are processed **in reverse order for responses** — the pipeline reverses the pipe list when `DataFlowDirection == RESPONSE`.

### Polymorphic JSON (critical pattern)

`Operation` and `Precondition` are interfaces deserialized via `@JsonTypeInfo` on the `method` field:
- Operations: `copy`, `mapper`, `regex_replace`, `set`, `save_state` (plus `Hl7v2*` variants)
- Preconditions: `equals`, `not_equals`, `exists`, `not_exists`, `regex_match` (plus `Hl7v2*` variants)

**Adding a new Operation or Precondition requires registering it in `@JsonSubTypes` on the interface.**

### Repository Layer

Two interchangeable backends selected by `spring.database`:
- **`file`** (default / local dev) — JSON files in `testing/configuration/`
- **`dynamodb`** — AWS DynamoDB Enhanced Client; activated by `spring.database=dynamodb`
- **`migrate`** — reads file, writes DynamoDB (one-time migration)

`RepositoryFactory` is the abstraction; `DynamoDbRepositoryFactory` and
`FileRepositoryFactory` are `@ConditionalOnExpression`-gated implementations.

DynamoDB entities use `@DynamoDbBean`. Complex fields (e.g., `List<Pipe>`, `List<Precondition>`)
are stored as JSON strings because DynamoDB Enhanced Client only handles primitive collections.
Each such field needs a matching `@DynamoDbAttribute` getter/setter pair that serializes/deserializes
via Jackson — see `Pipeline.getPipesJson()` as the pattern.

### Management API

All entities have a `*ApiController` under `xform.api`. Controllers extend `BaseApiController`
which provides cursor-based pagination (`nextCursor`/`prevCursor` + `limit`) and
`includeInactive` filtering. All API responses follow `{ "data": [...], "has_more": "true"|"false" }`.

### Security

- JWT authentication (`shared-secret` or JWKS via `xform.jwt.*`)
- mTLS for console connections (`XFORM_SERVICE_ENDPOINT_CRT_PATH` etc.)
- `XformPrincipalService` resolves roles from JWT claims (`roles`/`scope`)
- `AccessControl` model governs per-org permissions

### Logging / Advice

`@CaptureXformAdvice` (AOP) on `PipelineRunnerService.execute()` captures structured
transformation events to Elasticsearch (`elastic.*` config). `Advisable` / `Transformable`
marker interfaces are required on the annotated class.

---

## Key Conventions

- **`BaseModel`** — all stored entities extend it; provides `id` (UUID), `active` flag, and audit fields.
- **`OrganizationAware`** — interface for entities scoped to an org; required for org-based filtering.
- **Configuration is loaded at startup** from `XformConfig` (file mode) or DynamoDB; pipelines are matched at runtime by `findPipelineByContext(context)`.
- **Version field on `Solution`** — `Pipe` references a solution by UUID + version string; the runtime `Solution` wrapper (in `solutions/`) is distinct from the stored `model/Solution`.
- **`save_state` operation** — stores intermediate values into `ServiceContext.state` (a `HashMap<String, String>`) for use by downstream operations in the same pipeline run.
- **Test configuration** — local dev uses JSON files in `testing/configuration/`. Changes to entity schemas must be reflected there to keep tests passing.
- **`xform.allow-delete-via-api: false`** — hard-coded safe default; deletion via REST is disabled in production config.

---

## Environment Variables (key ones)

| Variable | Purpose |
|---|---|
| `XFORM_HUB_DESTINATION` | Upstream Hub endpoint URL |
| `XFORM_IIS_DESTINATION` | Upstream IIS endpoint URL |
| `XFORM_JWT_SECRET` | HS256 shared secret for JWT validation |
| `XFORM_JWT_URI` | JWKS endpoint (alternative to shared secret) |
| `XFORM_CONFIGURATIONS_DIRECTORY` | Root path for file-mode JSON configs |
| `ELASTIC_API_KEY` / `ELASTIC_HOST` | Elasticsearch logging |
| `spring.database` | `file` (default), `dynamodb`, or `migrate` |

Full reference: `docs/CONFIGURATION_REFERENCE.md`
