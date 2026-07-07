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

By default the Bundle contains only the requested resource type (e.g., `Immunization`
records) and any warnings. Use `_include` to add related resources.

**Use case: I want everything — immunization records and all referenced resources**

```
GET /fhir/{destinationId}/Immunization?family=Smith&given=John&birthdate=2000-01-01
    &_include=*:*
    &_revinclude=Provenance:target
```

`_include=*:*` follows all references from all returned resources. Adding
`_revinclude=Provenance:target` also includes a `Provenance` record for each result
identifying the IIS as the data source.

**Use case: I want immunization records and the patient only**

```
GET /fhir/{destinationId}/Immunization?family=Smith&given=John&birthdate=2000-01-01
    &_include=Immunization:patient
```

**Use case: I want immunization records and the administering provider or organization**

```
GET /fhir/{destinationId}/Immunization?family=Smith&given=John&birthdate=2000-01-01
    &_include=Immunization:patient
    &_include=Immunization:performer
```

> In the response Bundle, directly matched resources have `search.mode = match`;
> included resources have `search.mode = include`; warnings have `search.mode = outcome`.


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
- `500 Internal Server Error` — unexpected failure

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
