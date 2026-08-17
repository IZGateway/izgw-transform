# FHIR API Reference

*Part of the [IZ Gateway FHIR Interface](index.md) documentation.*

The FHIR facade is implemented in `FhirController` (`gov.cdc.izgateway.xform.endpoints.fhir`).
All endpoints are rooted at `/fhir/{destinationId}/` where `{destinationId}` is the IIS
destination identifier registered in the IZ Gateway Hub.

---

## Operations

### Search

Search for patient immunization data by demographics.

```
GET /fhir/{destinationId}/Immunization
GET /fhir/{destinationId}/ImmunizationRecommendation
GET /fhir/{destinationId}/Patient
```

The resource type in the URL determines the query profile sent to the IIS:

| URL resource type | V2 profile | IIS query name |
|---|---|---|
| `Immunization` | Z34 | Request Immunization History |
| `ImmunizationRecommendation` | Z44 | Request Evaluated History and Forecast |
| `Patient` | Z34 | Request Immunization History |

In all cases, the IIS is queried for a patient chart using the supplied demographics.
The returned Bundle is then filtered to the requested resource type.

> **POST search:** The FHIR specification defines `POST /[type]/_search` (with an
> `application/x-www-form-urlencoded` body) as the standard POST search pattern.
> This service supports both `GET /[type]?params` and `POST /[type]/_search` per the
> FHIR R4 specification.

#### Query Parameters

See [FHIR Query → QBP Mapping](fhir-to-qbp.md) for the complete parameter list and
how each maps to the QPD segment fields in the IIS query message.

Minimum required parameters (either, not both):
- A patient identifier: `patient.identifier` (or `patient` as a reference)
- Patient name **and** birth date: `patient.family` + `patient.given` + `patient.birthdate`

Additional demographics (`patient.gender`, `patient.address.*`, `patient.phone`, etc.)
are passed to the IIS as query hints; their use depends on the IIS implementation.

> **`subject` accepted as an alias for `patient` (non-standard).** `subject` is **not** a
> defined search parameter for the Immunization resource in FHIR R4 or US Core. As a
> convenience, the service accepts `subject` as an alias for `patient` when its value is a
> `Patient` reference (e.g. `subject=Patient/...`) or a bare id — it is rewritten to
> `patient` before the query is built. An explicitly-typed non-Patient reference (e.g.
> `subject=Group/...`) is ignored. If both `patient` and `subject` are supplied, `patient`
> takes precedence. This is a vendor convenience to ease integration; **clients should send
> `patient`**, which is the conformant parameter.

#### Limiting result count

```
_count=N
```

Where `N` is between 1 and 10 (default 5). Maps to the `RCP` segment quantity limit
in the QBP message.

#### Including additional resources

**You get only what you ask for.** By default the Bundle contains the requested resource type
(e.g. `Immunization` records) and any warnings, and nothing else. The subject `Patient`, the
administering `Practitioner` and `Location`, the `Organization` behind
`ImmunizationRecommendation.authority`, and the evaluated-history `Immunization` on a recommendation
query are all **absent** unless you ask for them with `_include` or `_revinclude`.

A reference to a resource you did not ask for is still delivered in full — the `reference` value the
conversion produced, plus `identifier` and `display` where it has them. This service serves no read
endpoint for a reference target, so resolve it either by asking for the target with `_include`, or
against your own data using the `identifier` and `display`.

**Use case: I want immunization records, the patient, and the administering provider**

```
GET /fhir/{destinationId}/Immunization?family=Smith&given=John&birthdate=2000-01-01
    &_include=Immunization:patient
    &_include=Immunization:performer
```

Each `_include` is required. Without them you get the `Immunization` records alone.

**Use case: I want everything — immunization records and all referenced resources**

```
GET /fhir/{destinationId}/Immunization?family=Smith&given=John&birthdate=2000-01-01
    &_include=*:*
    &_include=Resource:source:*
```

`_include=*:*` follows all references from all returned resources, transitively. It reaches only
resources a returned entry points **at**; add `_include=Resource:source:*` for the conversion-created
resources that nothing references (see
[Conversion-created resources](rsp-to-fhir.md#searchset-entries)).

**Use case: I want the forecast plus the evaluated history it was computed from**

```
GET /fhir/{destinationId}/ImmunizationRecommendation?...
    &_include=ImmunizationRecommendation:patient
    &_revinclude=Immunization:patient
    &_include=Immunization:authority
```

The evaluated-history `Immunization` carry `protocolApplied.doseNumber`,
`protocolApplied.seriesDoses`, `protocolApplied.authority` and `programEligibility`, which the
`/Immunization` query cannot return because the Z32 response behind it does not carry the source OBX
segments. All three parameters matter:

- `_include=ImmunizationRecommendation:patient` is **not optional**. The `Immunization` reference the
  `Patient`, not the recommendation, so a `_revinclude` has nothing to resolve from until the
  `Patient` is in the Bundle.
- `_include=Immunization:authority` is what brings in the schedule `Organization` that
  `protocolApplied.authority` points at — it is registered on the `Immunization`, not on the
  `ImmunizationRecommendation`.

Filter on `search.mode = match` for the forecast alone.

#### Migrating from the previous behavior

Earlier builds returned referenced resources, and the evaluated history on a recommendation query,
without being asked. To get that Bundle back:

```
GET /fhir/{destinationId}/ImmunizationRecommendation?...&_include=*:*&_revinclude=Immunization
```

That returns the same entries, with one difference: a reference the conversion built without a
resource behind it — a `PractitionerRole` pointing at a `Practitioner`, for example — now keeps its
literal `reference` value instead of being reduced to `identifier` and `display`. No `_include`
recovers such a target, because there is no resource to return.

`_include=*:*` alone is not enough on a recommendation query: the evaluated history is reachable only
in reverse, which is why the `_revinclude` is there.

> In the response Bundle, directly matched resources have `search.mode = match`;
> included resources have `search.mode = include`; warnings have `search.mode = outcome`.
> Select `search.mode = match` to get just the resources you queried for.

#### Search parameter names

An `_include` or `_revinclude` naming a search path the conversion never registered matches nothing
and is not an error, so a typo fails silently. The registered names are:

| Parameter | Reaches |
|---|---|
| `_include=ImmunizationRecommendation:patient` | the subject `Patient` |
| `_include=ImmunizationRecommendation:authority` | the `Organization` the forecast cites |
| `_include=Immunization:patient` | the subject `Patient` |
| `_include=Immunization:authority` | the `Organization` behind `protocolApplied.authority` |
| `_include=Immunization:location` | the administering `Location` |
| `_include=Immunization:performer` | the administering `Practitioner` / `PractitionerRole` |
| `_include=Immunization:manufacturer` | the vaccine manufacturer `Organization` |
| `_revinclude=Observation` | the dose and forecast `Observation` |
| `_revinclude=Observation:part-of` | only the `Observation` linked to a dose |
| `_revinclude=Immunization` | the evaluated-history doses |
| `_include=Resource:source:<type>` | conversion-created resources of that type, or `*` for all |

`_include` follows references forward; `_revinclude` finds resources pointing **at** something already
in the Bundle, so it needs a forward `_include` first unless the requested type is itself the target.
`_revinclude` never reaches a resource retained only by `_include=Resource:source:...`.


#### Response

A `Bundle` of type `searchset`. Any IIS error or warning is returned as an
`OperationOutcome` entry with `search.mode = outcome`.

---

### Read

Retrieve a previously found resource by its ID.

```
GET /fhir/{destinationId}/Immunization/{id}
GET /fhir/{destinationId}/ImmunizationRecommendation/{id}
GET /fhir/{destinationId}/Patient/{id}
```

The `{id}` is a Base64-encoded token returned by a prior search or `$match` operation.
The service decodes the ID to reconstruct the original patient identifier, re-queries the
IIS, and returns the specific resource from the resulting chart.

> **Note:** `ImmunizationRecommendation` resources are forecast data and may change over
> time. A read on a recommendation ID that was valid at a prior date may return different
> content or `404 Not Found` if the forecast has been updated.

Responses:
- `200 OK` — resource found, returns the single resource
- `404 Not Found` — resource not found; body is an `OperationOutcome`

---

### Patient/$match

Perform a probabilistic patient match query against the IIS.

```
POST /fhir/{destinationId}/Patient/$match
```

Request body must be either:
- A `Patient` resource containing the search demographics, or
- A `Parameters` resource with:
  - `resource` — a `Patient` resource
  - `onlySingleMatch` (boolean, optional) — return at most one result
  - `onlyCertainMatches` (boolean, optional) — return only high-confidence matches
  - `count` (integer 1–10, optional) — maximum number of results

The service converts the `Patient` resource fields to the same QPD parameters used by
the search operation (see [FHIR Query → QBP Mapping](fhir-to-qbp.md)) and sends a Z34
query to the IIS. Results are scored by match quality and returned as a `Bundle` of
`Patient` resources.

Responses:
- `200 OK` — Bundle of matched Patient resources with match scores
- `400 Bad Request` — body is `OperationOutcome` describing invalid input
- `422 Unprocessable Entity` — too many matches / no certain match (see below)
- `500 Internal Server Error` — unexpected failure

#### Too many matches (no certain match)

When the IIS reports "too much data found" (a Z33 response with query status
`TM` in QAK-2) and returns no patient records, `$match` does **not** return a
Bundle. Instead it returns HTTP `422 Unprocessable Entity` with a top-level
`OperationOutcome`:

```json
{
  "resourceType": "OperationOutcome",
  "issue": [{
    "severity": "warning",
    "code": "multiple-matches",
    "details": {
      "coding": [{ "system": "http://terminology.hl7.org/CodeSystem/v2-0208", "code": "TM" }],
      "text": "The matching operation found one or more possible matches, but did not find a certain match."
    }
  }]
}
```

This is the FHIR-sanctioned shape for an unsuccessful `$match` (particularly with
`onlyCertainMatches: true`): the operation cannot produce a certain match, and the
caller should refine the search demographics. The `details.text` phrase
`did not find a certain match` is matched literally by DIBBs Query Connector to
display its "No Certain Match Found" message and must not be reworded. The v2
table 0208 `TM` coding is retained in `details.coding` for provenance.

A true no-match (query status `NF`, no data found) still returns a `200 OK`
`searchset` Bundle with zero `Patient` entries, so consumers can distinguish
"no records" from "too many matches — refine the search". Search and read
endpoints are unaffected; this shape applies to `$match` only.

---

### Capabilities (metadata)

Return the server's FHIR R4 `CapabilityStatement` (the FHIR *capabilities* interaction).

```
GET /fhir/{destinationId}/metadata
```

FHIR R4 requires servers to publish a `CapabilityStatement` at `[base]/metadata`. Clients
that auto-detect server features — for example the DIBBs Query Connector — read this
document to discover that the service supports the Patient `$match` operation.

The document is **the same for every `{destinationId}`**: no destination lookup is
performed, and an unrecognized destination still receives the `CapabilityStatement`
(there is no `404` for this endpoint). It advertises:

- `status = active`, `fhirVersion = 4.0.1`, `format` including `application/fhir+json`
- the remaining elements R4 requires for a valid resource: a fixed `date`,
  `kind = instance`, and `implementation.description` (required when `kind` is
  `instance`)
- a single `rest` entry (`mode = server`) listing `Patient`, `Immunization`, and
  `ImmunizationRecommendation` with the `search-type` interaction
- the Patient `$match` operation (`rest.resource[type=Patient].operation`) named `match`
  with `definition = http://hl7.org/fhir/OperationDefinition/Patient-match`

Minimal example:

```json
{
  "resourceType": "CapabilityStatement",
  "status": "active",
  "date": "2026-07-30T00:00:00Z",
  "kind": "instance",
  "implementation": { "description": "IZ Gateway Transformation Service" },
  "fhirVersion": "4.0.1",
  "format": ["application/fhir+json"],
  "rest": [{
    "mode": "server",
    "resource": [
      { "type": "Patient",
        "interaction": [{ "code": "search-type" }],
        "operation": [{ "name": "match",
          "definition": "http://hl7.org/fhir/OperationDefinition/Patient-match" }] },
      { "type": "Immunization", "interaction": [{ "code": "search-type" }] },
      { "type": "ImmunizationRecommendation", "interaction": [{ "code": "search-type" }] }
    ]
  }]
}
```

Responses:
- `200 OK` — the `CapabilityStatement` (`application/fhir+json` by default)

---

## Connection Test

SMART on FHIR and other auth clients test connectivity with:

```
GET /fhir/{destinationId}/Patient?_summary=count&_count=1
```

The service short-circuits this call and returns an empty `searchset` Bundle with
`total=100` without contacting the IIS.

---

## Response Formats

All endpoints accept an `Accept` header to select the format.

| Accept header | Format |
|---|---|
| `application/fhir+json` | FHIR JSON (preferred) |
| `application/fhir+xml` | FHIR XML |
| `application/fhir+yaml` | FHIR YAML |
| `application/json` | JSON alias |
| `application/xml` | XML alias |
| `text/xml` | XML alias |

---

## Error Handling

| Condition | HTTP status | Body |
|---|---|---|
| IIS SOAP fault | `200` with `OperationOutcome` entry in Bundle | Bundle |
| Invalid query parameters | `400 Bad Request` | `OperationOutcome` |
| Resource not found (read) | `404 Not Found` | `OperationOutcome` |
| HL7 parse error | `500` | exception propagated |
| Unexpected exception | `500` | exception propagated |

---

## Authentication

Callers must authenticate with a valid client certificate trusted by the Transformation
Service and hold one of the following roles:

- `XFORM_SENDING_SYSTEM`
- `ADMIN`

See [QUICK_START.md](../QUICK_START.md) for certificate and configuration details.
