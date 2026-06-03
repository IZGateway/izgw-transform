# FHIR Query → QBP Mapping

*Part of the [IZ Gateway FHIR Interface](index.md) documentation.*

When a FHIR search or `Patient/$match` request arrives, the Transformation Service
builds an HL7 V2.5.1 `QBP_Q11` message and populates the `QPD` segment (Patient
Demographics Query) from the FHIR search parameters. This document describes that
mapping.

Reference: [CDC HL7 Version 2.5.1 Implementation Guide for Immunization Messaging §3.3](https://repository.immregistries.org/files/resources/5bef530428317/hl7_2_5_1_release_1_5__2018_update.pdf)

---

## QBP Message Structure

Every IIS query uses message type `QBP_Q11`. The profile identifier in `MSH-21`
determines the type of history requested:

| Profile | Description |
|---|---|
| `Z34` | Request Immunization History — returns `Immunization` resources |
| `Z44` | Request Evaluated History and Forecast — returns `ImmunizationRecommendation` resources |

### Fixed MSH Fields

The following MSH fields are set by the service for every outbound query:

| Field | Value |
|---|---|
| MSH-9 (Message Type) | `QBP^Q11^QBP_Q11` |
| MSH-10 (Message Control ID) | ULID (unique per request) |
| MSH-12 (Version ID) | `2.5.1` |
| MSH-15 (Accept Ack Type) | `ER` |
| MSH-16 (App Ack Type) | `AL` |
| MSH-18 (Character Set) | *(default)* |
| MSH-21 (Profile Identifier) | `Z34` or `Z44`, namespace `CDCPHINVS` |
| MSH-3 (Sending Application) | Caller's principal name |
| MSH-4 (Sending Facility) | Caller's organization |
| MSH-5 (Receiving Application) | Configured `v2tofhir.receivingApplication` |
| MSH-6 (Receiving Facility) | Configured `v2tofhir.receivingFacility` |
| MSH-11 (Processing ID) | `P` (production) |

### Fixed QPD Fields

| Field | Value |
|---|---|
| QPD-1 (Message Query Name) | Profile code (`Z34` / `Z44`), text, system `CDCPHINVS` |
| QPD-2 (Query Tag) | ULID (unique per request) |

### Fixed RCP Fields

The `RCP` (Response Control Parameters) segment controls the number of records returned:

| Field | Value |
|---|---|
| RCP-2.1 (Quantity) | Value of `_count` parameter (default `5`, max `10`) |
| RCP-2.2 (Units) | `RD` (records), system `HL70126` |

---

## FHIR Search Parameter → QPD Field Mapping

### Minimum Required

A valid query must supply **at least one** of:
1. A patient identifier (`patient.identifier` or `patient` as a reference)
2. Patient name (`patient.family` + `patient.given`) **and** birth date (`patient.birthdate`)

Omitting both causes a `400 Bad Request`.

### Parameter Table

| FHIR Search Parameter | QPD Field | CDC Guide Field Name | Notes |
|---|---|---|---|
| `patient` *(reference)* | QPD-3 | PatientList | Reference value decoded to `system\|value`; type forced to `MR` |
| `patient.identifier` | QPD-3 | PatientList | Repeatable; format `system\|value` |
| `patient.family` | QPD-4.1 | PatientName — Family | One value; name type `L` (legal) |
| `patient.given` | QPD-4.2 / QPD-4.3 | PatientName — Given / Middle | First occurrence → given name; second → middle name |
| `patient.suffix` | QPD-4.4 | PatientName — Suffix | One value |
| `patient.mothers-maiden-name` | QPD-5 | PatientMothersMaidenName | Family name component; name type `M` |
| `patient.mothers-maiden-name-given` | QPD-5 (given) | PatientMothersMaidenName | Round-trip support only |
| `patient.mothers-maiden-name-suffix` | QPD-5 (suffix) | PatientMothersMaidenName | Round-trip support only |
| `patient.birthdate` | QPD-6 | PatientDateOfBirth | Normalized to `yyyyMMdd` |
| `patient.gender` | QPD-7 | PatientSex | FHIR code first character; `U` if blank |
| `patient.address` | QPD-8.1 / QPD-8.2 | PatientAddress — Street | Street address line(s); address type `L` (legal) |
| `patient.address-city` | QPD-8.3 | PatientAddress — City | One value |
| `patient.address-state` | QPD-8.4 | PatientAddress — State | One value |
| `patient.address-postalcode` | QPD-8.5 | PatientAddress — Postal Code | One value |
| `patient.address-country` | QPD-8.6 | PatientAddress — Country | One value |
| `patient.phone` | QPD-9 | PatientHomePhone | Telephone number string |
| `patient.multipleBirth-indicator` | QPD-10 | PatientMultipleBirthIndicator | `Y` or `N` |
| `patient.multipleBirth-order` | QPD-11 | PatientBirthOrder | Integer birth order |
| `_count` | RCP-2 | *(quantity limit)* | Integer 1–10; default 5 |
| `_summary`, other `_` params | *(ignored)* | — | Except `_count`; special `_summary=count` triggers connection test |

---

## QPD Segment Layout

The table below shows the full QPD field layout as specified in the CDC guide and how
the Transformation Service populates each field.

| QPD Field | CDC Field Name | Population Source |
|---|---|---|
| QPD-1 | MessageQueryName | Profile code (Z34 or Z44) |
| QPD-2 | QueryTag | ULID |
| QPD-3 | PatientList (CX) | `patient` / `patient.identifier` |
| QPD-4 | PatientName (XPN) | `patient.family`, `patient.given`, `patient.suffix` |
| QPD-5 | PatientMothersMaidenName (XPN) | `patient.mothers-maiden-name` |
| QPD-6 | PatientDateOfBirth (TS) | `patient.birthdate` |
| QPD-7 | PatientSex (IS) | `patient.gender` |
| QPD-8 | PatientAddress (XAD) | `patient.address*` |
| QPD-9 | PatientHomePhone (XTN) | `patient.phone` |
| QPD-10 | PatientMultipleBirthIndicator (ID) | `patient.multipleBirth-indicator` |
| QPD-11 | PatientBirthOrder (NM) | `patient.multipleBirth-order` |

---

## Patient/$match Parameter Extraction

When a `Patient/$match` request is received, the service extracts QPD parameters from
the submitted `Patient` resource as follows:

| Patient resource field | FHIR search parameter used |
|---|---|
| `Patient.identifier` | `patient.identifier` |
| `Patient.name[0].family` | `patient.family` |
| `Patient.name[0].given` | `patient.given` (one call per given name) |
| `Patient.name[0].suffix` | `patient.suffix` |
| Extension: `mothers-maiden-name` | `patient.mothers-maiden-name` |
| `Patient.birthDate` | `patient.birthdate` |
| `Patient.gender` | `patient.gender` |
| `Patient.address[0].line` | `patient.address` |
| `Patient.address[0].city` | `patient.address-city` |
| `Patient.address[0].state` | `patient.address-state` |
| `Patient.address[0].postalCode` | `patient.address-postalcode` |
| `Patient.address[0].country` | `patient.address-country` |
| `Patient.multipleBirth` (boolean) | `patient.multipleBirth-indicator` |
| `Patient.multipleBirth` (integer) | `patient.multipleBirth-order` |

---

## Example QBP Message

The following is a representative QBP_Q11 Z34 message generated for:

```
GET /fhir/dev/Immunization
    ?patient.family=Johnson
    &patient.given=James
    &patient.birthdate=2016-04-14
    &patient.gender=male
    &patient.address=123 Main Street
    &patient.address-city=New Orleans
    &patient.address-state=LA
    &patient.address-postalcode=70115
    &patient.phone=555-5551111
```

```
MSH|^~\&|CallerPrincipal|CallerOrg|IIS_App|IIS_Facility|20260528143000||QBP^Q11^QBP_Q11|01JXXXXXXXXXXXXXXXXXX|P|2.5.1|||ER|AL|||||Z34^CDCPHINVS
QPD|Z34^Request Immunization History^CDCPHINVS|01JXXXXXXXXXXXXXXXXXY|||Johnson^James^^^^L||20160414|M|123 Main Street^^New Orleans^LA^70115^^L|^PRN^PH^^^555^5551111
RCP|I|5^RD^HL70126
```
