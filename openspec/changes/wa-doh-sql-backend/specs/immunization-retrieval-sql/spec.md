## Purpose

Given a confirmed patient ID from the SQL patient matching step, retrieve all
associated immunization records from the configured SQL database table and return
them as a typed tabular result for downstream FHIR conversion.

## Requirements

### Requirement: Immunization Query by Patient ID

The SQL back-end SHALL execute a parameterized ANSI SQL query that retrieves all
immunization rows for a given patient ID from the configured immunization table.

#### Scenario: Records found for confirmed patient

WHEN a confirmed patient ID is passed to immunization retrieval  
THEN all immunization rows associated with that patient ID are returned  
AND each row includes all columns referenced in the active mapping configuration

#### Scenario: No immunization records found

WHEN the confirmed patient ID has no associated immunization rows  
THEN an empty result set is returned  
AND the connector returns a valid FHIR Bundle containing the Patient resource only

#### Scenario: Query is parameterized

WHEN the immunization query is executed  
THEN the patient ID is bound as a JDBC parameter (not string-concatenated)

---

### Requirement: Configurable Table and Column Names

The immunization table name and the patient foreign-key column name SHALL be
specified in the mapping configuration file rather than hard-coded.

#### Scenario: Custom table name applied

WHEN `sql.tables.immunization` is set in the mapping configuration  
THEN the immunization query targets that table name

#### Scenario: Default table and column names

WHEN no table names are specified in the mapping configuration  
THEN the connector uses documented default table and column names that match
the H2 test fixture schema

---

### Requirement: Result Representation

Immunization query results SHALL be represented as a list of typed row objects
(e.g., `List<Map<String, Object>>` or a dedicated `TabularRow` type) that preserve
column names and values for downstream processing by the tabular-to-FHIR converter.

#### Scenario: Column names preserved

WHEN an immunization query returns rows  
THEN each row's map keys match the database column names exactly (case-insensitive
comparison for downstream mapping lookups)
