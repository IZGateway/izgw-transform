## Purpose

Convert a tabular SQL result set into a FHIR Bundle using a declarative YAML
configuration that maps database column names to FHIRPath destination locations
within FHIR resources, with optional concept mapping for code translation.

## Requirements

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

### Requirement: Multi-Resource Bundle Construction

The converter SHALL assemble a FHIR Bundle of type `searchset` containing at
minimum one `Patient` resource and one `Immunization` resource per immunization row.

#### Scenario: Patient resource populated once

WHEN multiple immunization rows are processed for the same patient  
THEN only one `Patient` resource is created in the Bundle  
AND all Immunization resources reference it via `patient` element

#### Scenario: Immunization resource created per row

WHEN the SQL result set contains N immunization rows  
THEN the Bundle contains exactly N `Immunization` resources

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
