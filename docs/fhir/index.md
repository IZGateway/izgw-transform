# IZ Gateway Transformation Service — FHIR Interface

The IZ Gateway Transformation Service exposes a FHIR R4 API that allows any FHIR-capable
client to query an Immunization Information System (IIS). The service translates each FHIR
request into an HL7 Version 2.5.1 QBP message following the
[CDC HL7 Version 2.5.1 Implementation Guide for Immunization Messaging](https://repository.immregistries.org/files/resources/5bef530428317/hl7_2_5_1_release_1_5__2018_update.pdf),
forwards it to the target IIS through the IZ Gateway Hub, and converts the RSP response back
to FHIR resources.

## How It Works

```
FHIR Client
    │  GET /fhir/{destinationId}/Immunization?patient.family=...
    ▼
FhirController (izgw-transform)
    │  builds QBP_Q11 (Z34 or Z44)
    ▼
IZ Gateway Hub → IIS
    │  RSP_K11 response
    ▼
v2tofhir MessageParser
    │  converts RSP segments → FHIR Bundle
    ▼
FhirController
    │  adjusts resource IDs, filters to requested type
    ▼
FHIR Client receives Bundle (SearchSet)
```

The conversion library ([v2tofhir](https://github.com/IZGateway/v2tofhir)) performs the
segment-level mapping. This documentation covers the end-to-end behavior as seen by a
FHIR client.

## Documents

| Document | Description |
|---|---|
| [FHIR API Reference](fhir-api.md) | Endpoints, operations, query parameters, response format |
| [FHIR Query → QBP Mapping](fhir-to-qbp.md) | How each FHIR search parameter maps to QPD fields in the IIS query |
| [RSP → FHIR Resource Mapping](rsp-to-fhir.md) | How IIS response segments map to FHIR resources in the returned Bundle |

## Supported Resources

All resources are returned from a single IIS query. The primary resource type requested
determines whether a Z34 (Immunization History) or Z44 (Evaluated History and Forecast)
query is sent.

| FHIR Resource | Source V2 Segments | IIS Query Type |
|---|---|---|
| `Patient` | PID, PD1 | Z34 or Z44 |
| `Immunization` | ORC, RXA, RXR, OBX | Z34 |
| `ImmunizationRecommendation` | ORC, RXA, RXR, OBX | Z44 |
| `ServiceRequest` | ORC | Z34 or Z44 |
| `Organization` | MSH, ORC-21/23, PD1-4 | Z34 or Z44 |
| `Practitioner` | ORC-12/21, RXA-10, PV1-7/8/9, OBX-16/25 | Z34 or Z44 |
| `PractitionerRole` | ORC-12/21/23, OBX-15/16/25 | Z34 or Z44 |
| `Location` | ORC-29, RXA-11/27/28, PV1-3/6/11/37 | Z34 or Z44 |
| `RelatedPerson` | NK1 | Z34 or Z44 |
| `Encounter` | PV1 | Z34 or Z44 |
| `Account` | PID-18 | Z34 or Z44 |

## Resource ID Design

FHIR resource IDs returned by the service are stable, opaque, Base64-encoded strings
derived from the V2 identifiers in the IIS response. A `Patient` ID encodes
`system|value` from the first patient identifier. Non-patient resource IDs encode
`patientSystem|patientValue|resourceSystem|resourceValue`, tying every resource back
to the patient chart that produced it.

This means a resource retrieved today can be re-read tomorrow — as long as the IIS
still has the data — using a standard FHIR read (`GET /fhir/{dest}/{ResourceType}/{id}`).

## Conventions

- All endpoints are under `/fhir/{destinationId}/` where `destinationId` is the IIS
  destination identifier configured in the IZ Gateway Hub.
- Requests may use `GET`, `POST /_search`, or `HEAD`.
- Responses are available as `application/fhir+json`, `application/fhir+xml`,
  `application/fhir+yaml`, and several plain media type aliases.
- The service requires mTLS authentication. Callers must hold a role of
  `XFORM_SENDING_SYSTEM` or `ADMIN`.
