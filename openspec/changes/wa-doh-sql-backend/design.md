## Context

The IZ Gateway Transformation Service (`izgw-transform`) is a Spring Boot application
that converts FHIR queries into HL7 V2 messages and dispatches them to IIS back-ends
via IZ Gateway. The FHIR entry point is `FhirController` (`/fhir`), which converts
inbound FHIR parameters into a `QBP_Q11` query, sends it through `HubController` →
`IZGHubProducer` (an Apache Camel producer), receives a V2 response, and converts it
back to a FHIR Bundle using the `v2tofhir` library.

`IDIMatch` is a stateless utility class (all static methods) that scores HAPI FHIR
`Patient` objects against a search `Patient` using the HL7 IDI patient-matching
algorithm. It already operates on FHIR types — not on raw SQL rows or custom POJOs.

The proposed SQL back-end sits beside the existing IZ Gateway path. Both paths
share the same FHIR entry point and return FHIR Bundles. The only difference is
how the search is executed and how the Bundle is assembled.

---

## Goals / Non-Goals

**Goals:**
- Enable single-patient FHIR queries against a JDBC-connected SQL database
- Enable full async Bulk FHIR `$export` from the same SQL database
- Preserve 100% backward compatibility with the existing IZ Gateway path
- Require no code changes when no SQL datasource is configured
- Provide a working integration test fixture using H2 + existing CSV test data

**Non-Goals:**
- Supporting multiple simultaneous SQL databases (one datasource per deployment)
- Replacing the Apache Camel routing layer for the IZ Gateway path
- Implementing a full FHIR server on top of the SQL database
- Paginated single-patient query results (bulk handles population-scale queries)
- OAuth / SMART on FHIR scoping for the Bulk FHIR endpoint (v1)

---

## Decisions

### 1. Routing intercept point: FhirController, not Camel

**Decision**: Route `destination == "sql"` inside `FhirController` before the HL7 V2
message is built, calling a new `SqlFhirBackend` Spring service directly.

**Rationale**: The SQL path produces a FHIR Bundle directly — there is no HL7 V2
message involved. Inserting a Camel component would require bridging FHIR objects
through Camel's exchange body, adding complexity for no routing benefit. The existing
Camel layer is an IZ-Gateway-specific concern; SQL is not.

**Alternative considered**: New Camel `SqlComponent` in the routing graph. Rejected
because Camel's exchange model is oriented toward message bytes, not typed Java objects,
and the SQL path has no use for Camel's transformation or retry features.

---

### 2. IDIMatch does not need a new interface

**Decision**: Do not change `IDIMatch`'s method signatures. Instead, introduce a
`SqlPatientRowMapper` that converts a SQL result row (`Map<String, Object>`) into a
HAPI FHIR `Patient` object. Pass those `Patient` objects to the existing IDIMatch
scoring methods.

**Rationale**: `IDIMatch` already operates on FHIR `Patient` objects, which are a
well-defined model. Building a `Patient` from a SQL row is a straightforward field
mapping. Creating an `IPatientMatchData` interface would duplicate the FHIR `Patient`
API and provide no benefit.

**How the SQL patient search works**:
1. The inbound FHIR `$match` or `Patient?` parameters are already in a FHIR `Patient`
   (the search patient) — this is the query input.
2. `SqlPatientSearchService` executes a broad SQL query (matching on name + DOB, or
   DOB + gender, etc.) returning candidate rows.
3. Each candidate row is mapped to a FHIR `Patient` by `SqlPatientRowMapper`.
4. IDIMatch scores each candidate `Patient` against the search `Patient`.
5. The one candidate at or above threshold (singular match required) has its patient ID
   extracted for immunization retrieval.

---

### 3. Query interface abstraction: IQueryBackend

**Decision**: Extract `IQueryBackend` with a single method:

```java
Bundle query(Patient searchPatient, String destinationId, HttpServletRequest req)
    throws FaultException, HL7Exception, UnexpectedException, SecurityFault;
```

`FhirController` holds a list of `IQueryBackend` implementations ordered by priority,
selecting the first whose `supports(destinationId)` returns true.

**Rationale**: Minimal interface surface. The existing HubController/IIS path is wrapped
in `IzGatewayQueryBackend implements IQueryBackend`. `SqlFhirBackend implements IQueryBackend`
handles `"sql"`. The router in `FhirController` is a simple `for` loop.

**Why not a full POJO `IQueryRequest`?**: The search parameters are already captured
in a FHIR `Patient` resource (constructed by the existing parsing logic). Wrapping that
in an intermediate POJO before passing it to backends would add a translation layer with
no benefit. The `Patient` IS the query request for the matching path.

---

### 4. Tabular-to-FHIR conversion: FHIRPath-keyed YAML config

**Decision**: Define a YAML configuration (`sql-mapping.yml`) where each entry specifies:
- `column`: database column name (case-insensitive)
- `resource`: FHIR resource type (`Patient` or `Immunization`)
- `path`: FHIRPath expression relative to the resource root (e.g., `name.family`)
- `type`: FHIR datatype (`string`, `date`, `code`, `Coding`, `CodeableConcept`, etc.)
- `system` (optional): code system URI for Coding/CodeableConcept
- `concept_map` (optional): list of `{from, to}` pairs

Use the HAPI FHIR `FhirPathEngine` to resolve the target path on the resource and
set the value. For simple primitive paths, direct setter calls are acceptable as an
optimization for common types (name.family, birthDate, etc.).

**Rationale**: FHIRPath provides a standard, extensible way to address any element in
any FHIR resource without hard-coding getter/setter chains per column. The WA DOH
`all_vax_event` schema is fully covered by this mechanism using the enriched mapping
from `all_vax_event_enriched_mapping.csv`.

**Alternative considered**: Pure annotation-based Java mapping class. Rejected because
it requires redeployment for every schema change. The YAML config supports hot-reload.

---

### 5. Bulk FHIR job storage: in-memory (v1)

**Decision**: Store export jobs in a `ConcurrentHashMap` keyed by job UUID. NDJSON
output is written to `java.io.tmpdir` during generation and served from there.

**Rationale**: v1 scope is WA DOH's single-instance Azure deployment. In-memory
job state survives normal operation. Temp files are cleaned up on job DELETE or
application restart.

**Risk**: Jobs are lost on restart. Mitigation: document this limitation; WA DOH
can re-kick exports if needed. A persistent job store (DynamoDB or SQL) is a natural
v2 enhancement.

**Alternative considered**: DynamoDB job table (consistent with existing repository
layer). Deferred to v2 to keep scope bounded.

---

### 6. Reserved destination enforcement

**Decision**: Add `"sql"` to a `ReservedDestinations` constant set in
`XformConstants`. The `Destination` validation logic checks this set and rejects
registration of a destination with a reserved name.

---

## Risks / Trade-offs

| Risk | Mitigation |
|---|---|
| SQL injection via mapping config | All query parameters bound via `JdbcTemplate` named parameters; column names come from config (not user input), validated at startup |
| H2 dialect incompatibility with SQL Server | Use ANSI SQL-only features in `SqlPatientSearchService`; test with both dialects in CI |
| IDI match threshold too aggressive (many no-matches) | Default threshold configurable; document recommended values for WA DOH dataset |
| In-memory Bulk FHIR jobs lost on restart | Document; WA DOH's use case is scheduled batch runs, not real-time |
| Large NDJSON files blocking temp storage | Apply configurable max-rows-per-file; default to chunked output (e.g., 10,000 rows/file) |
| FHIRPath evaluation performance for large exports | Pre-resolve common paths to direct setter calls; profile with 6,000-row test dataset |

---

## Migration Plan

1. **No migration required for existing deployments** — the SQL back-end activates
   only when `spring.datasource.url` is present. Existing deployments without
   datasource config are unaffected.
2. **WA DOH deployment**: Add datasource config block + `sql-mapping.yml` to
   `application.yml`. No restart of existing IZ Gateway connectivity required.
3. **Rollback**: Remove `spring.datasource.*` from config and restart. SQL endpoints
   become 503. No data is affected.

---

## Open Questions

1. **Authentication for Bulk FHIR**: Should `$export` require a specific role beyond
   `XFORM_SENDING_SYSTEM`? WA DOH may need a dedicated bulk-export role.
2. **Patient ID namespace**: What system URI should be used for the `ASIIS_PAT_ID`
   identifier in the Patient resource? (e.g., `urn:oid:2.16.840.1.114222.4.1.xxx`)
3. **Chunked NDJSON file size**: Should the chunk size be configurable per deployment,
   or fixed at a well-tested default?
4. **`sql-mapping.yml` location in repo**: Ship a default WA DOH mapping in
   `src/main/resources/`; allow override via `sql.mapping.config-path` property.
5. **Table/column name configuration**: Does WA DOH need the table names to be
   configurable, or will the default names matching the H2 test fixture be sufficient?
