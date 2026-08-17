## ADDED Requirements

### Requirement: A searchset contains only what the caller asked for

The service SHALL return, in the response to a FHIR query, only entries the caller asked for:
resources of the requested type, `OperationOutcome` resources, and resources retained because they
satisfied an `_include` or `_revinclude` parameter the caller supplied. It SHALL NOT retain a
resource on any other ground. In particular, the service SHALL NOT retain a resource merely because
a retained entry references it, and SHALL NOT retain a resource merely because the HL7 v2 response
happened to carry it.

The shape of a response SHALL therefore be predictable from the query alone: the same query against
the same HL7 v2 response SHALL yield the same set of resource types regardless of which
organization sent it, because no organization configuration participates in searchset assembly.

This requirement is **BREAKING** for a caller relying on unrequested resources arriving. It governs
the FHIR REST inbound path only. The SOAP/HL7 v2 inbound message contract, the outbound query sent
to the downstream Hub or IIS, and the transformation pipeline (organizations, pipelines, solutions,
operations, and preconditions) SHALL be unchanged, and existing organization transformation
configurations SHALL continue to work untouched.

#### Scenario: a plain immunization query returns immunizations only
- **GIVEN** a request to `GET /fhir/{destination}/Immunization` with no `_include` or `_revinclude`
  parameter
- **WHEN** the converted bundle contains `Immunization` resources and the `Patient` they reference
- **THEN** the returned searchset SHALL contain the `Immunization` entries with
  `search.mode = "match"`
- **AND** it SHALL NOT contain the `Patient`

#### Scenario: a plain recommendation query returns the forecast only
- **GIVEN** a request to `GET /fhir/{destination}/ImmunizationRecommendation` with no `_include` or
  `_revinclude` parameter
- **WHEN** the converted bundle contains an `ImmunizationRecommendation`, the `Patient`, the
  schedule `Organization` behind `authority`, `Immunization` resources from the evaluated history,
  and forecast `Observation` resources
- **THEN** the returned searchset SHALL contain only the `ImmunizationRecommendation` entries, with
  `search.mode = "match"`

#### Scenario: the caller asks for the subject patient
- **GIVEN** the same immunization query
- **WHEN** the caller supplies `_include=Immunization:patient`
- **THEN** the `Patient` SHALL be retained with `search.mode = "include"`

#### Scenario: the caller asks for everything the returned resources reference
- **GIVEN** a request to `GET /fhir/{destination}/ImmunizationRecommendation`
- **WHEN** the caller supplies `_include=*:*`
- **THEN** every resource reachable by a reference from a retained entry SHALL be retained with
  `search.mode = "include"`

#### Scenario: the caller asks for the evaluated history and its evaluation data
- **GIVEN** a Z42 response containing administered doses and forecasts for one patient
- **WHEN** the caller issues `GET /fhir/{destination}/ImmunizationRecommendation` with
  `_include=ImmunizationRecommendation:patient`, `_revinclude=Immunization:patient`, and
  `_include=Immunization:authority`
- **THEN** the `Patient` and one `Immunization` per administered dose SHALL be retained with
  `search.mode = "include"`
- **AND** the `Organization` behind `protocolApplied.authority` SHALL be retained, because the
  authority is registered on the `Immunization` and not on the `ImmunizationRecommendation`
- **AND** the `ImmunizationRecommendation` SHALL be the only entry labelled `match`

#### Scenario: organization configuration does not change the returned types
- **GIVEN** two organizations whose pipelines apply different transformations
- **WHEN** each issues the same FHIR query and the HL7 v2 responses convert to bundles holding the
  same resource types
- **THEN** the two returned searchsets SHALL contain the same set of resource types

### Requirement: A reverse include resolves only from a retained resource

The service SHALL resolve a `_revinclude` parameter against the reverse references of resources
already retained in the searchset. A resource that references a retained entry SHALL therefore be
reached by `_revinclude` only when the entry it references is itself retained, and a caller SHALL be
able to retain that intermediate entry with `_include`.

This follows from how the HL7 v2 to FHIR conversion bookkeeps references: a reverse reference is
recorded on the resource being pointed at, so it is discoverable only by traversing that resource.
The consequence is observable and callers depend on it — the evaluated-history `Immunization` and the
forecast `Observation` both reference the `Patient` rather than the `ImmunizationRecommendation`, so
neither is reachable on a recommendation query until the `Patient` is retained.

Any retained resource that the sought resource references SHALL serve as the anchor; the anchor need
not be the one whose search name the parameter names. The conversion keeps a single canonical
`Reference` per resource and accumulates every reverse search name onto it, so a qualified
`_revinclude` matches when the named search name is among the accumulated names, regardless of which
retained resource the reverse reference was found on. A search name the conversion never registered
SHALL match nothing.

#### Scenario: a reverse include finds nothing when the referenced entry is absent
- **GIVEN** a request to `GET /fhir/{destination}/ImmunizationRecommendation` with
  `_revinclude=Observation` and no `_include`
- **WHEN** the forecast `Observation` resources reference the `Patient`, which is not retained
- **THEN** no `Observation` entry SHALL be retained
- **AND** the request SHALL succeed

#### Scenario: retaining the intermediate entry makes the reverse include resolve
- **GIVEN** the same request with `_include=ImmunizationRecommendation:patient` added
- **WHEN** the searchset is assembled
- **THEN** the `Patient` SHALL be retained with `search.mode = "include"`
- **AND** every `Observation` referencing it SHALL be retained with `search.mode = "include"`

#### Scenario: another retained resource serves as the anchor
- **GIVEN** a request to `GET /fhir/{destination}/ImmunizationRecommendation` with
  `_include=ImmunizationRecommendation:authority` and `_revinclude=Immunization:patient`, and no
  `_include` naming `patient`
- **WHEN** the evaluated-history `Immunization` reference both the `Patient`, which is not retained,
  and the schedule `Organization`, which is
- **THEN** those `Immunization` entries SHALL be retained with `search.mode = "include"`
- **AND** the `Patient` SHALL NOT be retained

#### Scenario: an unregistered reverse search name matches nothing
- **GIVEN** the same request with `_revinclude=Immunization:nosuchsearchname`
- **WHEN** the searchset is assembled
- **THEN** no `Immunization` entry SHALL be retained
- **AND** the request SHALL succeed

#### Scenario: the evaluated history is reached through the subject patient
- **GIVEN** a Z42 response and a request to `GET /fhir/{destination}/ImmunizationRecommendation`
- **WHEN** the caller supplies `_include=ImmunizationRecommendation:patient` and
  `_revinclude=Immunization:patient`
- **THEN** one `Immunization` per administered dose SHALL be retained with `search.mode = "include"`

### Requirement: A reference to a resource outside the searchset is left intact

The service SHALL leave every `Reference` on a retained entry as the HL7 v2 to FHIR conversion
produced it. Where the referenced resource is not present in the returned searchset, the service
SHALL NOT remove, rewrite, or reduce the `reference` element, and SHALL NOT alter the reference's
`identifier` or `display`. Resolving such a reference is the caller's decision: the literal value,
the identifier, and the display text are all delivered, and the caller may resolve the reference
against its own source of truth, request the target with `_include`, or ignore it.

This restores the reference handling callers saw before this capability existed, and it applies to
mandatory 1..1 references — `Immunization.patient`, `ImmunizationRecommendation.patient` — as much
as to optional ones.

The service makes no guarantee that every delivered reference is readable. Where the HL7 v2 to FHIR
conversion produces a `Reference` carrying no `reference`, no `identifier`, and no `display`, the
service SHALL deliver it as produced. Populating such a reference is the conversion's concern, not
the searchset's.

#### Scenario: a mandatory reference keeps its literal value
- **GIVEN** a request to `GET /fhir/{destination}/Immunization` with no `_include` parameter
- **WHEN** the returned `Immunization` entries carry a `patient` reference to a `Patient` that the
  searchset does not contain
- **THEN** each `patient` reference SHALL retain the `reference` value the conversion produced
- **AND** its `identifier` and `display` SHALL be unchanged

#### Scenario: an optional reference to an omitted resource is left alone
- **GIVEN** a returned recommendation searchset
- **WHEN** the retained `ImmunizationRecommendation` carries an `authority` reference to an
  `Organization` the searchset does not contain
- **THEN** that reference SHALL be delivered unchanged

#### Scenario: a reference the conversion left empty is delivered as produced
- **GIVEN** a converted bundle in which a retained entry holds a `Reference` with no `reference`, no
  `identifier`, and no `display`
- **WHEN** the searchset is assembled
- **THEN** that reference SHALL be delivered unchanged
- **AND** the searchset SHALL NOT be rejected on that account

#### Scenario: asking for the target changes nothing about the reference
- **GIVEN** the same immunization query
- **WHEN** the caller supplies `_include=Immunization:patient` so the `Patient` is retained
- **THEN** the `patient` reference SHALL carry the same value it carries when the `Patient` is
  omitted
- **AND** it SHALL resolve to the retained `Patient`

## MODIFIED Requirements

### Requirement: Conversion-created resources are retained only when white-listed

Resources that the HL7 v2 to FHIR conversion synthesises as a side effect of datatype and message
parsing — rather than from a dedicated segment the caller queried for — SHALL be removed from the
searchset unless the caller white-lists them, because the enriched reference they are the target of
already carries an identifier and display text sufficient for production use.

The caller SHALL be able to white-list them with the `_include=Resource:source:<type>` parameter,
where `<type>` is a resource type or `*` for all such resources; a white-listed resource SHALL be
retained with `search.mode = "include"`. Such a resource SHALL also be retained when a forward
`_include` reaches it as the target of a reference on a retained entry, on the same terms as any
other resource — so `_include=Immunization:location` retains the conversion-created `Location`
without the `Resource:source` form.

A `_revinclude` SHALL NOT reach a resource retained only by this white-list. A white-listed resource
is retained but is not itself traversed, so no reverse reference is resolved from it.

#### Scenario: conversion-created resources are removed by default
- **GIVEN** a query whose converted bundle contains conversion-created `Practitioner` and `Location`
  resources that no retained entry references
- **WHEN** the caller supplies no `_include=Resource:source:...` parameter
- **THEN** those entries SHALL NOT appear in the returned searchset

#### Scenario: a caller white-lists conversion-created resources by type
- **GIVEN** the same query
- **WHEN** the caller supplies `_include=Resource:source:Practitioner`
- **THEN** the conversion-created `Practitioner` entries SHALL be retained with
  `search.mode = "include"`

#### Scenario: a caller white-lists all conversion-created resources
- **GIVEN** the same query
- **WHEN** the caller supplies `_include=Resource:source:*`
- **THEN** every conversion-created resource SHALL be retained with `search.mode = "include"`

#### Scenario: a forward include reaches a conversion-created resource
- **GIVEN** a query whose converted bundle contains a conversion-created `Location` that a retained
  `Immunization` references
- **WHEN** the caller supplies `_include=Immunization:location` and no `Resource:source` parameter
- **THEN** that `Location` SHALL be retained with `search.mode = "include"`

#### Scenario: a reverse include does not reach a white-listed resource
- **GIVEN** a query whose converted bundle contains conversion-created `Provenance` resources, which
  reference a conversion-created `DocumentReference` rather than a resource of the requested type
- **WHEN** the caller supplies `_revinclude=Provenance`, with or without
  `_include=Resource:source:DocumentReference`
- **THEN** no `Provenance` entry SHALL be retained
- **AND** the request SHALL succeed

### Requirement: Unclassified entries are removed

The service SHALL remove from the returned searchset every entry that no requirement in this
capability retains, whether or not a retained entry references it. A caller SHALL therefore never
receive an entry whose `search.mode` is absent.

#### Scenario: an unreferenced, unrequested resource is removed
- **GIVEN** a converted bundle containing a resource that is not of the requested type, is not an
  `OperationOutcome`, satisfies no `_include` or `_revinclude` parameter, and is referenced by no
  retained entry
- **WHEN** the searchset is assembled
- **THEN** that entry SHALL NOT appear in the returned searchset

#### Scenario: being referenced does not save an entry from removal
- **GIVEN** a converted bundle containing a resource that satisfies no `_include` or `_revinclude`
  parameter but is referenced by a retained entry
- **WHEN** the searchset is assembled
- **THEN** that entry SHALL NOT appear in the returned searchset
- **AND** the reference to it SHALL be delivered unchanged, per "A reference to a resource outside
  the searchset is left intact"

#### Scenario: forecast observations are removed from a plain recommendation query
- **GIVEN** a request to `GET /fhir/{destination}/ImmunizationRecommendation` with no `_include` or
  `_revinclude` parameter
- **WHEN** the converted bundle contains `Observation` resources carrying the forecast detail
- **THEN** those `Observation` entries SHALL NOT appear in the returned searchset

## REMOVED Requirements

### Requirement: A returned searchset contains no dangling references

**Reason**: The guarantee was bought by returning resources the caller did not ask for — the
subject `Patient` on every query, and the schedule `Organization` on a recommendation query — which
made the shape of a response unpredictable from the query. Its fallback behavior, reducing an
unretainable reference to `identifier` and `display`, also silently discarded the literal reference
value the conversion produced. Both are replaced by "A searchset contains only what the caller asked
for" and "A reference to a resource outside the searchset is left intact": the searchset may now
contain a reference to a resource it does not hold, exactly as it did before this capability
existed.

**Migration**: A caller that relied on the referenced resource arriving unasked SHALL request it —
`_include=Immunization:patient`, `_include=ImmunizationRecommendation:patient`,
`_include=ImmunizationRecommendation:authority`, or `_include=*:*` for all of them. A caller that
resolves references locally within the bundle SHALL either request the targets with `_include` or
resolve the reference's `identifier` and `display` against its own source of truth; the service
serves no read endpoint for a reference target, so a literal reference value in a returned searchset
is not fetchable from this service.

### Requirement: A recommendation query returns the evaluated history it was sent with

**Reason**: The evaluated history is not what an `ImmunizationRecommendation` query asked for.
Returning it unasked contradicts "A searchset contains only what the caller asked for", and the
`include` labelling it used already told the caller these entries were not hits — so the caller can
ask for them by the ordinary means instead.

**Migration**: A caller that wants the Z42 evaluated history and the evaluation data it carries —
`protocolApplied.doseNumber` and `seriesDoses` (OBX `30973-2` / `59782-3`),
`protocolApplied.authority` (OBX `59779-9`), and `programEligibility` (OBX `64994-7`) — SHALL send
`_include=ImmunizationRecommendation:patient&_revinclude=Immunization:patient`, adding
`_include=Immunization:authority` to resolve `protocolApplied.authority` within the searchset, or
`_include=*:*&_revinclude=Immunization` to obtain everything the pre-change service returned. The
`Immunization` resources are reached through the `Patient` they reference, so retaining the `Patient`
with `_include` is required for the `_revinclude` to reach them, per "A reverse include resolves only
from a retained resource". This data remains reachable through no other call: the `/Immunization`
path sends Z34 and receives Z32, which carries none of those OBX codes.
