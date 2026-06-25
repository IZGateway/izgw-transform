## Purpose

Generalize the existing IDIMatch patient-matching algorithm to operate on a
database-sourced data interface, and implement a SQL-backed patient search that
uses IDIMatch scoring to identify a single best-match patient before immunization
records are retrieved.

## Requirements

### Requirement: SqlPatientRowMapper

`SqlPatientRowMapper extends SqlTableMapper<Patient>` SHALL convert a SQL result
row (`Map<String, Object>`) to a HAPI FHIR `Patient` object using the shared
column-to-FHIR conversion infrastructure in `SqlTableMapper<T>`. `IDIMatch` is
not modified — it continues to operate on FHIR `Patient` objects exactly as it
does today.

#### Scenario: SQL row converted to FHIR Patient

WHEN a SQL query returns a candidate patient result row  
THEN `SqlPatientRowMapper` delegates column mapping to `SqlTableMapper<T>` base
logic, producing a FHIR `Patient` with demographics populated from the row  
AND the resulting `Patient` is passed directly to `IDIMatch` without any change
to the algorithm

#### Scenario: Existing IDIMatch callers unaffected

WHEN `SqlPatientRowMapper` is introduced  
THEN all existing callers of `IDIMatch` that pass FHIR `Patient` objects continue
to compile and behave identically — no interface changes are made to `IDIMatch`

---

### Requirement: SQL Patient Search Query

The SQL back-end SHALL execute a parameterized ANSI SQL query against the
configured patient table using the demographic data from the search `Patient`.
When a `_lastUpdated` `DateRangeParam` is present, a timestamp predicate SHALL
be added to the WHERE clause against the column declared `is_last_updated: true`
in the mapping configuration (see `temporal-query-filtering` spec). This predicate
is applied in SQL — not as a post-filter.

#### Scenario: Query uses parameterized SQL

WHEN a patient search is executed  
THEN all patient-supplied values are bound as JDBC parameters (not string-concatenated)  
AND the query is protected against SQL injection

#### Scenario: Candidate rows returned for scoring

WHEN the patient table contains rows matching on at least one indexed demographic field  
THEN those rows are returned as candidate matches for IDIMatch scoring

---

### Requirement: Singular Match Enforcement

IDIMatch SHALL score all candidate rows. The SQL back-end SHALL proceed to
immunization retrieval only when exactly one candidate meets the configured
match threshold.

#### Scenario: Single match found — proceed

WHEN exactly one candidate row scores at or above the match threshold  
THEN its patient ID is extracted and passed to immunization retrieval

#### Scenario: No match found — return empty

WHEN no candidate row meets the match threshold  
THEN the connector returns an empty FHIR Bundle (no immunizations, no error)

#### Scenario: Multiple matches found — return ambiguous response

WHEN two or more candidate rows score at or above the match threshold  
THEN the connector returns a response indicating an ambiguous match  
AND does NOT return immunization data

#### Scenario: Match threshold configurable

WHEN the `sql.matching.threshold` configuration property is set  
THEN IDIMatch uses that value as the minimum acceptable score  
AND the default threshold SHALL be the same value used by the existing IZ Gateway path
