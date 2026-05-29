# RSP → FHIR Resource Mapping

*Part of the [IZ Gateway FHIR Interface](index.md) documentation.*

When the IIS returns an RSP_K11 response message, the v2tofhir `MessageParser` converts
each V2 segment into one or more FHIR resources. This document describes those mappings
at the field level.

Reference: [CDC HL7 Version 2.5.1 Implementation Guide for Immunization Messaging](https://repository.immregistries.org/files/resources/5bef530428317/hl7_2_5_1_release_1_5__2018_update.pdf)

---

## Bundle Structure

The `MessageParser` converts the entire RSP message into a single FHIR `Bundle`. After
conversion, `FhirController` performs two post-processing steps:

1. **`adjustIdentifiers`** — rewrites each resource's FHIR ID to an opaque, stable,
   Base64-encoded token derived from the V2 identifiers (see
   [Resource ID Design](index.md#resource-id-design)).
2. **`filter`** — reduces the bundle to the requested resource type (plus any resources
   requested via `_include` / `_revinclude`), sets `Bundle.type = searchset`, and marks
   each entry with `search.mode`.

The segment parsers that produce each resource type are listed below.

---

## MSH → MessageHeader / Organization / Endpoint / Bundle

**Parser:** `MSHParser`

| V2 Field | FHIR Target | Notes |
|---|---|---|
| MSH-3 | `MessageHeader.source` (Endpoint) | Sending application → source endpoint |
| MSH-3 | `Organization` (sender) | Sending application name |
| MSH-4 | `Organization` (sender facility) | Sending facility |
| MSH-5 | `MessageHeader.destination` (Endpoint) | Receiving application |
| MSH-6 | `Organization` (receiver facility) | Receiving facility |
| MSH-7 | `Bundle.timestamp` | Message date/time |
| MSH-9.1 | `MessageHeader.meta.tag` | Message code |
| MSH-9.2 | `MessageHeader.eventCoding` | Trigger event |
| MSH-9.3 | `Bundle.implicitRules`, `MessageHeader.definition` | Message structure profile |
| MSH-10 | `Bundle.identifier` | Message control ID |
| MSH-21 | `Bundle.meta.profile` | Message profile identifier |
| MSH-22 | `MessageHeader.responsible` | Sending responsible organization |
| MSH-23 | `Organization` (destination receiver) | Receiving responsible organization |
| MSH-8 / MSH-26 / MSH-27 / MSH-28 | `MessageHeader.meta.security` | Security labels |

---

## QAK → OperationOutcome

**Parser:** `QAKParser`

| V2 Field | FHIR Target | Notes |
|---|---|---|
| QAK-1 | `OperationOutcome` (meta.tag, system `QueryTag`) | Query tag echo |
| QAK-2 | `OperationOutcome.issue.details` | Query response status (`OK`, `NF`, `AE`, `AR`) |
| QAK-2 | `OperationOutcome.issue.code` / `.severity` | Derived from status code |

Query response codes:
- `OK` — data found; `information` / `informational`
- `NF` — not found; `warning` / `not-found`
- `AE` / `AR` — application/access error; `error` / `processing`

---

## QPD → Parameters (echo)

**Parser:** `QPDParser`

The query parameters are echoed back in the RSP. The parser converts the QPD into a
FHIR `Parameters` resource. For the QBP request side it also emits a
`Bundle.entry.request` with a reconstructed GET URL; for the RSP side it emits a
`Bundle.entry.response.location`.

---

## PID → Patient / RelatedPerson / Account

**Parser:** `PIDParser`

### Patient fields

| V2 Field | FHIR Element | Notes |
|---|---|---|
| PID-2, PID-3, PID-4, PID-20 | `Patient.identifier` | All patient identifier lists |
| PID-5 | `Patient.name` | Legal name (name use `usual`) |
| PID-6 | Extension: `patient-mothers-maiden-name` | Mother's maiden name |
| PID-7 | `Patient.birthDate` | Date portion |
| PID-7 | Extension: `patient-birth-time` | Time portion, if present |
| PID-8 | `Patient.gender` | Mapped from HL7 administrative sex table |
| PID-9 | `Patient.name` (alias) | Alias name |
| PID-10 | US Core Race Extension | Race category and detail |
| PID-11 | `Patient.address` | Legal address (address type `L`) |
| PID-12 | `Patient.address.district` | County code |
| PID-13 | `Patient.telecom` | Home phone(s) |
| PID-14 | `Patient.telecom` | Business phone(s) |
| PID-15 | `Patient.communication.language` | Primary language |
| PID-16 | `Patient.maritalStatus` | Marital status code |
| PID-17 | Extension: `patient-religion` | Religion code |
| PID-19 | `Patient.identifier` | SSN identifier |
| PID-23 | Extension: `patient-birthPlace` | Birth place |
| PID-24 | `Patient.multipleBirthBoolean` | Multiple birth indicator |
| PID-25 | `Patient.multipleBirthInteger` | Birth order |
| PID-26, PID-39 | Extension: `patient-citizenship` | Citizenship |
| PID-28 | Extension: `patient-nationality` | Nationality |
| PID-29 | `Patient.deceasedDateTime` | Date/time of death |
| PID-30 | `Patient.deceasedBoolean` | Deceased indicator |
| PID-33 | `Patient.meta.lastUpdated` | Last update date/time |
| PID-40 | `Patient.telecom` | Other telecom |

### RelatedPerson (mother)

| V2 Field | FHIR Element | Notes |
|---|---|---|
| PID-21 | `RelatedPerson.identifier` | Mother's identifier |

### Account

| V2 Field | FHIR Element | Notes |
|---|---|---|
| PID-18 | `Account.identifier` | Patient account number |

---

## NK1 → RelatedPerson

**Parser:** `NK1Parser`

| V2 Field | FHIR Element | Notes |
|---|---|---|
| NK1-2 | `RelatedPerson.name` | Next-of-kin name |
| NK1-3, NK1-7 | `RelatedPerson.relationship` | Relationship codes |
| NK1-4, NK1-32 | `RelatedPerson.address` | Address |
| NK1-5, NK1-31, NK1-40, NK1-41 | `RelatedPerson.telecom` | Phone/contact |
| NK1-6 | `RelatedPerson.telecom` | Business phone |
| NK1-8, NK1-9 | `RelatedPerson.period` | Effective period start/end |
| NK1-12, NK1-33 | `RelatedPerson.identifier` | Identifiers |
| NK1-15 | `RelatedPerson.gender` | Administrative sex |
| NK1-16 | `RelatedPerson.birthDate` | Date of birth |
| NK1-20 | `RelatedPerson.communication.language` | Language |
| NK1-30 | `RelatedPerson.name` | Mother's maiden name |
| NK1-37 | `RelatedPerson.identifier` | SSN |

---

## ORC → ServiceRequest / Practitioner / Organization

**Parser:** `ORCParser`

### ServiceRequest

| V2 Field | FHIR Element | Notes |
|---|---|---|
| ORC-1 | `ServiceRequest.status` | Order control code mapping |
| ORC-2, ORC-33 | `ServiceRequest.identifier` | Placer order number |
| ORC-3 | `ServiceRequest.identifier` | Filler order number (primary identifier) |
| ORC-4 | `ServiceRequest.identifier` | Placer group number |
| ORC-5 | `ServiceRequest.status` | Order status |
| ORC-8 | `ServiceRequest.identifier` | Parent order number |
| ORC-9 | `ServiceRequest.authoredOn` | Date/time of transaction |
| ORC-28 | `ServiceRequest.meta.security` | Confidentiality code |
| ORC-29 | `ServiceRequest.locationCode` | Order location |

### Practitioner (requester)

| V2 Field | FHIR Element | Notes |
|---|---|---|
| ORC-12 | `Practitioner` / `ServiceRequest.requester` | Ordering provider |
| ORC-21 | `Organization` / `ServiceRequest.requester` | Ordering organization |
| ORC-22 | `Organization.address` | Ordering organization address |
| ORC-23 | `Organization.telecom` | Ordering organization phone |

---

## RXA → Immunization / ImmunizationRecommendation

**Parser:** `RXAParser`

The same RXA segment populates either an `Immunization` (Z34 response) or
`ImmunizationRecommendation` (Z44 response) resource depending on the query profile.

### Immunization fields

| V2 Field | FHIR Element | Notes |
|---|---|---|
| RXA-3 | `Immunization.occurrenceDateTime` | Date/time of administration |
| RXA-5 | `Immunization.vaccineCode` | Vaccine administered code (CVX) |
| RXA-6 | `Immunization.doseQuantity.value` | Dose amount |
| RXA-7 | `Immunization.doseQuantity.unit` / `.code` | Dose units |
| RXA-10 | `Immunization.performer` (Practitioner) | Administering provider |
| RXA-11 | `Immunization.location` (Location) | Administering location |
| RXA-15 | `Immunization.lotNumber` | Lot number |
| RXA-16 | `Immunization.expirationDate` | Expiration date |
| RXA-17 | `Immunization.manufacturer` (Organization) | Vaccine manufacturer |
| RXA-18 | `Immunization.statusReason` / `status = not-done` | Refusal/exemption reason |
| RXA-19 | `Immunization.reasonCode` | Indication (e.g., catch-up) |
| RXA-20 | `Immunization.status` | Completion status |
| RXA-21 | `Immunization.status` | Action code |
| RXA-22 | `Immunization.recorded` | Date vaccine information recorded |
| RXA-27 | `Immunization.location` (Location) | Administered-at location |
| RXA-28 | `Location.address` | Location address |

### ImmunizationRecommendation fields

| V2 Field | FHIR Element | Notes |
|---|---|---|
| RXA-3 | `recommendation.dateCriterion` | Forecast date |
| RXA-5 | `recommendation.vaccineCode` | Recommended vaccine code |
| RXA-22 | `recommendation.dateCriterion` | Next dose date |

---

## RXR → Immunization route and site

**Parser:** `RXRParser`

| V2 Field | FHIR Element | Notes |
|---|---|---|
| RXR-1 | `Immunization.route` | Route of administration (HL7 table 0162) |
| RXR-2 | `Immunization.site` | Body site (HL7 table 0163) |

---

## OBX → Observation / Immunization / ImmunizationRecommendation

**Parser:** `OBXParser`

OBX segments carry supplemental observations about an administered vaccine (Z34) or
forecast recommendation (Z44). The observation type code (`OBX-3`) determines whether
the value is attached to the parent `Immunization`, folded into the
`ImmunizationRecommendation`, or emitted as a standalone `Observation`.

### General observation fields

| V2 Field | FHIR Element | Notes |
|---|---|---|
| OBX-2 | *(type discriminator)* | Data type (NM, ST, CE, TS, etc.) |
| OBX-3 | `Observation.code` | Observation identifier (LOINC or local) |
| OBX-5 | `Observation.value[x]` | Value, type-dependent |
| OBX-6 | `Observation.valueQuantity.unit` | Unit of measure |
| OBX-7 | `Observation.referenceRange` | Reference range |
| OBX-8 | `Observation.interpretation` | Abnormal flag |
| OBX-10 | Extension: `abnormal-test` | Abnormal test indicator |
| OBX-11 | `Observation.status` | Observation status |
| OBX-14 | `Observation.effectiveDateTime` | Observation date/time |
| OBX-15 | `Observation.performer` (Organization) | Responsible organization identifier |
| OBX-16 | `Observation.performer` (Practitioner) | Responsible practitioner |
| OBX-17 | `Observation.method` | Method |
| OBX-18 | `Observation.device` | Device identifier |
| OBX-19 | `Observation.issued` | Analysis date/time |
| OBX-20 | `Observation.bodySite` | Body site |
| OBX-21 | `Observation.identifier` | Observation identifier |
| OBX-23 | `Organization.name` | Performing organization name |
| OBX-24 | `Organization.address` | Performing organization address |
| OBX-25 | `Practitioner` | Medical director |
| OBX-33 | `Specimen.identifier` | Specimen identifier |

### Immunization-specific OBX codes

Certain LOINC or CDC PHIN-coded OBX observations are applied directly to the parent
`Immunization` resource:

| OBX-3 code | FHIR target | Description |
|---|---|---|
| `64994-7` (LOINC) | `Immunization.fundingSource` | Vaccine funding source |
| `30956-7` (LOINC) | `Immunization.education` | Vaccine information statement (VIS) |
| `VFC eligibility` | `Immunization.programEligibility` | VFC eligibility category |

### ImmunizationRecommendation forecast OBX codes (Z44 response)

| OBX-3 code | FHIR target | Description |
|---|---|---|
| `59779-9` (LOINC) | `recommendation.forecastStatus` | Series status |
| `59780-7` (LOINC) | `recommendation.doseNumber` | Dose number in series |
| `59781-5` (LOINC) | `recommendation.seriesDoses` | Recommended number of doses |
| `59782-3` (LOINC) | `recommendation.series` | Series name |
| `30956-7` (LOINC) | `recommendation.vaccineCode` | Recommended vaccine code |
| Next dose date codes | `recommendation.dateCriterion` | Earliest/recommended/latest dates |

---

## PV1 → Encounter / Location

**Parser:** `PV1Parser`

### Encounter fields

| V2 Field | FHIR Element | Notes |
|---|---|---|
| PV1-2 | `Encounter.class` / `Encounter.type` | Patient class |
| PV1-4 | `Encounter.type` | Admission type |
| PV1-5 | `Encounter.identifier` | Pre-admit number |
| PV1-7, PV1-8, PV1-9, PV1-17, PV1-52 | `Encounter.participant` (Practitioner) | Attending / referring / consulting / admitting providers |
| PV1-10 | `Encounter.serviceType` | Hospital service |
| PV1-13 | `Encounter.hospitalization.reAdmission` | Re-admission indicator |
| PV1-14 | `Encounter.hospitalization.admitSource` | Admit source |
| PV1-15 | `Encounter.extension` (ambulatory status) | Ambulatory status |
| PV1-16 | `Encounter.extension` (VIP indicator) | VIP indicator |
| PV1-19 | `Encounter.identifier` | Visit number |
| PV1-36 | `Encounter.hospitalization.dischargeDisposition` | Discharge disposition |
| PV1-38 | `Encounter.hospitalization.dietPreference` | Diet preference |
| PV1-44 | `Encounter.period.start` | Admit date/time |
| PV1-45 | `Encounter.period.end` | Discharge date/time |
| PV1-50 | `Encounter.identifier` | Alternate visit ID |
| PV1-54 | `Encounter.episodeOfCare` | Episode of care |

### Location fields

| V2 Field | FHIR Element | Source Segments | Notes |
|---|---|---|---|
| PV1-3 | `Location` (assigned) | PV1 | Current bed/room |
| PV1-6 | `Location` (prior) | PV1 | Prior location |
| PV1-11 | `Location` (temporary) | PV1 | Temporary location |
| PV1-37 | `Location` (discharged to) | PV1 | Discharge destination |
| PV1-40 | `Location.operationalStatus` | PV1 | Bed status |
| PV1-42 | `Location` (pending) | PV1 | Pending location |
| PV1-43 | `Location` (prior temporary) | PV1 | Prior temporary location |
| ORC-29 | `Location` | ORC | Order location |
| RXA-11 | `Location` | RXA | Administering location |
| RXA-27 | `Location` | RXA | Alternate administering location |
| RXA-28 | `Location.address` | RXA | Administering location address |

---

## Full RSP Segment → FHIR Resource Summary

| V2 Segment | Primary FHIR Resource | Also Produces |
|---|---|---|
| MSH | `MessageHeader` | `Organization`, `Endpoint`, `Bundle` (timestamp/id) |
| QAK | `OperationOutcome` | — |
| QPD | `Parameters` | — |
| PID | `Patient` | `RelatedPerson` (mother), `Account` |
| PD1 | `Patient` (extensions) | `Organization` (primary care), `Practitioner` (PCP) |
| NK1 | `RelatedPerson` | `Patient` (reference) |
| ORC | `ServiceRequest` | `Practitioner`, `Organization` |
| RXA | `Immunization` or `ImmunizationRecommendation` | `Practitioner`, `Organization`, `Location` |
| RXR | `Immunization` (route/site) | — |
| OBX | `Observation` | `Immunization` (extensions), `ImmunizationRecommendation` (forecast), `Practitioner`, `Organization` |
| EVN | — | `Practitioner` (EVN-5), `Location` (EVN-7) |
| PV1 | `Encounter` | `Location`, `Practitioner` |
| OBR | `ServiceRequest` | — |
| DSC | *(continuation)* | — |
