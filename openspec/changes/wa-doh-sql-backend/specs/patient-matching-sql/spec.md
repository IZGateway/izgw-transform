## Purpose

Generalize the existing IDIMatch patient-matching algorithm to operate on a
database-sourced data interface, and implement a SQL-backed patient search that
uses IDIMatch scoring to identify a single best-match patient before immunization
records are retrieved.

## Requirements

### Requirement: IPatientMatchData Interface

The IDIMatch algorithm SHALL be refactored to accept an `IPatientMatchData`
interface rather than a concrete class, with methods covering all demographic
fields it currently evaluates (name, DOB, gender, address, phone, MRN, etc.).

#### Scenario: Existing callers unaffected

WHEN the refactor is applied  
THEN all existing callers of IDIMatch that pass a concrete type implementing
`IPatientMatchData` continue to compile and behave identically

#### Scenario: SQL row wrapped as IPatientMatchData

WHEN a SQL query returns a patient result row  
THEN it is wrapped in an adapter implementing `IPatientMatchData`  
AND passed to IDIMatch without modification to the algorithm itself

---

### Requirement: SQL Patient Search Query

The SQL back-end SHALL execute a parameterized ANSI SQL query against the
configured patient table using the demographic data from `IQueryRequest`.

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
