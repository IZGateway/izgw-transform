# fhir-searchset-filtering Specification

## Purpose

This capability defines how the IZ Gateway Transformation Service turns a converted HL7 v2 response
into a FHIR R4 searchset for a FHIR query caller: which entries the response bundle retains, what
`Bundle.entry.search.mode` each retained entry carries, how `_include` and `_revinclude` are
resolved, and the integrity guarantee that no retained entry references a resource the bundle does
not contain. It applies to every FHIR query endpoint the service exposes and is independent of the
transformation pipeline, which does not participate in searchset assembly.

## Requirements

### Requirement: FHIR query responses are returned as a searchset

The service SHALL return the response to a FHIR query as a `Bundle` of type `searchset`, regardless
of the bundle type produced by the HL7 v2 to FHIR conversion. Every retained entry SHALL carry a
populated `Bundle.entry.search.mode`. The requested resource type SHALL be determined from the
request path, and for the `Patient/$match` operation the requested type SHALL be `Patient`.

This requirement governs the FHIR REST inbound path only. The SOAP/HL7 v2 inbound path and the
outbound query sent to the downstream Hub or IIS SHALL be unchanged, as SHALL the transformation
pipeline (organizations, pipelines, solutions, operations, and preconditions) — no organization
configuration affects searchset assembly.

#### Scenario: response bundle is typed as a searchset
- **GIVEN** a query to a FHIR query endpoint that produces a response bundle
- **WHEN** the service returns the response to the caller
- **THEN** `Bundle.type` SHALL be `searchset`

#### Scenario: every retained entry carries a search mode
- **GIVEN** a returned searchset
- **WHEN** the caller inspects any entry in the bundle
- **THEN** that entry SHALL have a populated `Bundle.entry.search.mode`

#### Scenario: the $match operation resolves to the Patient type
- **GIVEN** a request to `POST /fhir/{destination}/Patient/$match`
- **WHEN** the response searchset is assembled
- **THEN** the requested resource type SHALL be `Patient`, and `Patient` resources SHALL be
  classified as described in "Resources of the requested type are labelled `match`"

### Requirement: Resources of the requested type are labelled `match`

The service SHALL set `search.mode = "match"` on, and SHALL retain, every entry whose resource type
equals the resource type requested by the query. Resources of any other type SHALL NOT be labelled
`match`.

#### Scenario: requested type is labelled match
- **GIVEN** a request to `GET /fhir/{destination}/ImmunizationRecommendation`
- **WHEN** the converted bundle contains one `ImmunizationRecommendation`
- **THEN** that entry SHALL be retained with `search.mode = "match"`

#### Scenario: a resource of another type is not labelled match
- **GIVEN** a request to `GET /fhir/{destination}/ImmunizationRecommendation`
- **WHEN** the converted bundle also contains `Immunization` resources produced from the same
  HL7 v2 response
- **THEN** those `Immunization` entries SHALL NOT be labelled `match`
- **AND** they SHALL be retained only if another requirement in this capability requires it

### Requirement: `OperationOutcome` entries are labelled `outcome`

The service SHALL retain every `OperationOutcome` in the converted bundle and SHALL set
`search.mode = "outcome"` on it, so conversion warnings and errors always reach the caller.

#### Scenario: conversion warnings survive filtering
- **GIVEN** a converted bundle containing one or more `OperationOutcome` resources
- **WHEN** the searchset is assembled
- **THEN** each `OperationOutcome` entry SHALL be retained with `search.mode = "outcome"`

### Requirement: `_include` and `_revinclude` results are labelled `include`

The service SHALL set `search.mode = "include"` on every resource retained because it satisfied an
`_include` or `_revinclude` parameter. It SHALL NOT label such a resource `match`. In FHIR R4,
`include` is the defined value for an entry "added to the results because of a join", and clients
filter on `mode = "match"` to obtain the hits; labelling joined resources `match` makes the hits
indistinguishable from their supporting resources.

An `_include` or `_revinclude` parameter SHALL be resolved against the search-parameter names the
HL7 v2 to FHIR conversion registered for each reference. A parameter naming a resource type or
search name that is not registered SHALL match nothing and SHALL NOT be an error. Wildcard (`*`)
resource types and search names SHALL match any value.

This is a **BREAKING** change to the labelling a client observes: a client that previously read
`_include` and `_revinclude` results as `match` SHALL now read them as `include`.

#### Scenario: a reverse-included resource is labelled include
- **GIVEN** a request to `GET /fhir/{destination}/ImmunizationRecommendation` with
  `_revinclude=Observation`
- **WHEN** the converted bundle contains one `ImmunizationRecommendation` and supporting
  `Observation` resources that reference it
- **THEN** the `ImmunizationRecommendation` entry SHALL have `search.mode = "match"`
- **AND** every retained `Observation` entry SHALL have `search.mode = "include"`
- **AND** the caller SHALL be able to obtain exactly the requested resources by selecting entries
  with `search.mode = "match"`

#### Scenario: a forward-included resource is labelled include
- **GIVEN** a request to a FHIR query endpoint with an `_include` parameter that matches a reference
  on a resource of the requested type
- **WHEN** the searchset is assembled
- **THEN** the referenced resource SHALL be retained with `search.mode = "include"`

#### Scenario: an unmatched include parameter is not an error
- **GIVEN** a request with an `_include` or `_revinclude` parameter naming a search name that the
  conversion does not register for any reference
- **WHEN** the searchset is assembled
- **THEN** the request SHALL succeed
- **AND** no additional entry SHALL be retained on account of that parameter

### Requirement: A returned searchset contains no dangling references

No entry retained in a returned searchset SHALL carry a `Reference.reference` element pointing at a
resource that the searchset does not contain. When a resource would otherwise be removed but is
still referenced by a retained entry, the service SHALL retain it with `search.mode = "include"`.
Where the referenced resource cannot be retained because the searchset never held it, the service
SHALL instead clear the `reference` element, preserving `Reference.identifier` and
`Reference.display` so the target remains readable as a logical reference. A reference reduced this
way still satisfies a 1..1 cardinality, because the element itself remains present.

FHIR R4 permits this without the client asking for it: "the server has the prerogative to return
additional search results if it believes them to be relevant." The defect this closes is that
mandatory 1..1 references — `Immunization.patient`, `ImmunizationRecommendation.patient` — and the
populated `ImmunizationRecommendation.authority` resolved to nothing in the delivered bundle and to
no endpoint this service serves, so a client performing local reference resolution rather than a
follow-up fetch could not resolve them.

This requirement SHALL take precedence over "Conversion-created resources are retained only when
white-listed": a conversion-created resource that a retained entry references SHALL be retained.

#### Scenario: the subject Patient is retained for an immunization query
- **GIVEN** a request to `GET /fhir/{destination}/Immunization` with no `_include` parameter
- **WHEN** the retained `Immunization` entries carry a `patient` reference to a `Patient` in the
  converted bundle
- **THEN** that `Patient` SHALL be retained with `search.mode = "include"`
- **AND** every `Immunization.patient` reference in the searchset SHALL resolve to it

#### Scenario: the subject Patient is retained for a recommendation query
- **GIVEN** a request to `GET /fhir/{destination}/ImmunizationRecommendation` with no `_include`
  parameter
- **WHEN** the retained `ImmunizationRecommendation` carries a `patient` reference to a `Patient` in
  the converted bundle
- **THEN** that `Patient` SHALL be retained with `search.mode = "include"`

#### Scenario: the schedule Organization behind authority is retained
- **GIVEN** a request to `GET /fhir/{destination}/ImmunizationRecommendation` with no `_include`
  parameter
- **WHEN** the retained `ImmunizationRecommendation` carries an `authority` reference to an
  `Organization` created by the conversion from the immunization schedule used
- **THEN** that `Organization` SHALL be retained with `search.mode = "include"`, notwithstanding
  that it is a conversion-created resource

#### Scenario: no reference in a returned searchset dangles
- **GIVEN** any returned searchset
- **WHEN** every `Reference.reference` element on every retained entry is resolved against the
  entries of that same searchset
- **THEN** every such reference SHALL resolve to an entry present in the searchset

#### Scenario: an unretainable target is reduced to a logical reference
- **GIVEN** a retained entry carrying a reference to a resource the converted bundle never held —
  for example a reference the HL7 v2 to FHIR conversion did not register in its reference
  bookkeeping, so no target resource is available to retain
- **WHEN** the searchset is assembled
- **THEN** that reference SHALL have no `reference` element
- **AND** its `identifier` and `display` SHALL be preserved
- **AND** the searchset SHALL still satisfy "no reference in a returned searchset dangles"

#### Scenario: retaining a referenced resource does not promote it to a match
- **GIVEN** a request to `GET /fhir/{destination}/ImmunizationRecommendation`
- **WHEN** a `Patient` and an `Organization` are retained solely to satisfy reference integrity
- **THEN** neither SHALL be labelled `match`
- **AND** selecting entries with `search.mode = "match"` SHALL yield only the
  `ImmunizationRecommendation` resources

### Requirement: A recommendation query returns the evaluated history it was sent with

On a query for `ImmunizationRecommendation`, the service SHALL retain the `Immunization` resources
converted from the same response, with `search.mode = "include"`. It SHALL NOT label them `match`,
so a client can still isolate the forecast it asked for by selecting `search.mode = "match"`.

A Z42 response ("Return Evaluated History and Forecast") carries the patient's evaluated history
alongside the forecast, split by RXA-5: `998^No Vaccine Administered` becomes a `recommendation`
component, any other CVX code becomes an `Immunization`. Those `Immunization` resources carry
evaluation data reachable through no other call — `protocolApplied.doseNumber` and `seriesDoses`
(OBX `30973-2` / `59782-3`), `protocolApplied.authority` (OBX `59779-9`), and `programEligibility`
(OBX `64994-7`). The `/Immunization` path does not compensate, because it sends Z34 and receives
Z32, which carries none of those OBX codes.

This applies only to the recommendation query path. On an `/Immunization` query the same resources
are the matches, and their labelling SHALL be unchanged.

#### Scenario: evaluated history accompanies the forecast
- **GIVEN** a Z42 response containing administered doses and forecasts for one patient
- **WHEN** the client issues `GET /fhir/{destination}/ImmunizationRecommendation` with no
  `_include` parameter
- **THEN** one `Immunization` per administered dose SHALL be retained with
  `search.mode = "include"`
- **AND** the single `ImmunizationRecommendation` SHALL be the only entry labelled `match`

#### Scenario: included history carries the evaluation data
- **GIVEN** a returned recommendation searchset containing evaluated history
- **WHEN** the client reads an included `Immunization`
- **THEN** the `protocolApplied.doseNumber`, `protocolApplied.seriesDoses`,
  `protocolApplied.authority` and `programEligibility` values present in the source response SHALL
  be populated
- **AND** `protocolApplied.authority` SHALL resolve to an `Organization` in the same searchset

#### Scenario: each included dose keeps its own identifier
- **GIVEN** a Z42 response with more than one administered dose
- **WHEN** the searchset is assembled
- **THEN** each included `Immunization` SHALL carry the filler order number of its own ORC-3
- **AND** no two entries in the searchset SHALL collide on resource type and id

#### Scenario: an immunization query is unaffected
- **GIVEN** a client issuing `GET /fhir/{destination}/Immunization`
- **WHEN** the searchset is assembled
- **THEN** the `Immunization` resources SHALL be labelled `match`, not `include`

### Requirement: Conversion-created resources are retained only when white-listed

Resources that the HL7 v2 to FHIR conversion synthesises as a side effect of datatype and message
parsing — rather than from a dedicated segment the caller queried for — SHALL be removed from the
searchset unless the caller white-lists them, because the enriched reference they are the target of
already carries an identifier and display text sufficient for production use.

The caller SHALL be able to white-list them with the `_include=Resource:source:<type>` parameter,
where `<type>` is a resource type or `*` for all such resources; a white-listed resource SHALL be
retained with `search.mode = "include"`. Resources referenced by a retained entry SHALL be retained
regardless of this requirement, per "A returned searchset contains no dangling references".

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

### Requirement: Unclassified entries are removed

The service SHALL remove from the returned searchset every entry that no requirement in this
capability retains. A caller SHALL therefore never receive an entry whose `search.mode` is absent.

#### Scenario: an unreferenced, unrequested resource is removed
- **GIVEN** a converted bundle containing a resource that is not of the requested type, is not an
  `OperationOutcome`, satisfies no `_include` or `_revinclude` parameter, and is referenced by no
  retained entry
- **WHEN** the searchset is assembled
- **THEN** that entry SHALL NOT appear in the returned searchset

#### Scenario: forecast observations are removed from a plain recommendation query
- **GIVEN** a request to `GET /fhir/{destination}/ImmunizationRecommendation` with no `_include` or
  `_revinclude` parameter
- **WHEN** the converted bundle contains `Observation` resources carrying the forecast detail
- **THEN** those `Observation` entries SHALL NOT appear in the returned searchset
- **AND** the returned `ImmunizationRecommendation` SHALL remain self-contained — it SHALL NOT
  reference any of the removed `Observation` resources
