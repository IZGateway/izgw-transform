## Purpose

Define the shared column-to-FHIR conversion infrastructure (`SqlTableMapper<T>`),
its typed specializations (`SqlPatientRowMapper`, `SqlImmunizationRowMapper`), and
the Bundle assembler (`TabularFhirConverter`) that uses them to convert a tabular
SQL result set into a FHIR Bundle via a declarative YAML mapping configuration.

## Requirements

### Requirement: SqlTableMapper Base Class

`SqlTableMapper<T>` SHALL be an abstract class owning all column-to-FHIR
conversion logic shared across resource types. Concrete subclasses (`SqlPatientRowMapper`,
`SqlImmunizationRowMapper`) extend it to produce a specific FHIR resource type `T`.
`TabularFhirConverter` contains no type conversion logic of its own — it delegates
entirely to registered `SqlTableMapper<?>` instances.

#### Scenario: Shared conversion logic not duplicated

WHEN both `SqlPatientRowMapper` and `SqlImmunizationRowMapper` convert the same
FHIR datatype (e.g., `CodeableConcept`)  
THEN both delegate to the same implementation in `SqlTableMapper<T>` — no
duplication of converter code across subclasses

---

### Requirement: Column-to-FHIRPath Mapping Configuration

A YAML configuration file SHALL define, for each database column, the target
FHIR resource type, the FHIRPath expression identifying the destination element,
and the FHIR datatype to produce.

#### Scenario: String column mapped to FHIR string element

WHEN a column entry specifies `type: string` and a valid FHIRPath  
THEN the column value is placed at that path in the target resource as a FHIR string

#### Scenario: Date column mapped to FHIR date element

WHEN a column entry specifies `type: date` and a FHIRPath pointing to a date element  
THEN the column value is converted to an ISO 8601 date and placed at that path

#### Scenario: Column absent from configuration is ignored

WHEN a column returned by the SQL query has no entry in the mapping configuration  
THEN it is silently ignored (no error, no output)

#### Scenario: Configuration column absent from result set is skipped

WHEN a column named in the configuration is not present in the SQL result row  
THEN that mapping entry is skipped and no error is raised

---

### Requirement: Supported FHIR Output Datatypes

The converter SHALL support mapping a raw database value to each of these FHIR datatypes:
`string`, `integer`, `decimal`, `boolean`, `date`, `dateTime`, `code`, `Coding`,
`CodeableConcept`.

#### Scenario: code type mapping

WHEN a column entry specifies `type: code`  
THEN the raw value is placed as the `code` field of the target FHIR element

#### Scenario: Coding type mapping with system

WHEN a column entry specifies `type: Coding` and a `system` property  
THEN the output Coding has `system` set to the configured value and `code` set to the raw value

#### Scenario: CodeableConcept type mapping

WHEN a column entry specifies `type: CodeableConcept`, a `system`, and an optional `display`  
THEN the output CodeableConcept contains one Coding with system+code and the optional display text

---

### Requirement: Concept Mapping

A column entry MAY include a `concept_map` block that translates raw database values
to output codes before the FHIR datatype conversion is applied.

#### Scenario: Mapped value found

WHEN a column value matches a `from` entry in `concept_map`  
THEN the converter uses the corresponding `to` value as the output code

#### Scenario: Unmapped value — passthrough or null

WHEN a column value has no matching `from` entry  
AND no `default` is specified in `concept_map`  
THEN the raw value is passed through as-is

WHEN a `default` is specified  
THEN the default value is used for any unmatched raw value

---

### Requirement: Multi-Resource Bundle Assembly

`TabularFhirConverter` SHALL assemble a FHIR Bundle of type `searchset` by
delegating each row to the registered `SqlTableMapper<?>` for the appropriate
resource type. It contains no type conversion logic — its sole responsibility
is Bundle construction.

#### Scenario: Patient resource populated once

WHEN multiple immunization rows are processed for the same patient  
THEN `TabularFhirConverter` creates exactly one `Patient` resource in the Bundle
(via `SqlPatientRowMapper`) and all `Immunization` resources reference it via
the `patient` element

#### Scenario: Immunization resource created per row

WHEN the SQL result set contains N immunization rows  
THEN `TabularFhirConverter` delegates each row to `SqlImmunizationRowMapper`
and the Bundle contains exactly N `Immunization` resources

---

### Requirement: Configuration Hot-Reload

The mapping configuration SHALL be loadable from the filesystem (not only the
classpath) so that it can be updated without redeploying the application.

#### Scenario: External config file used when present

WHEN `sql.mapping.config-path` points to a file on the filesystem  
THEN that file is loaded in preference to any classpath default

#### Scenario: Classpath default used as fallback

WHEN no external config path is specified  
THEN the converter loads the default mapping from the application classpath
