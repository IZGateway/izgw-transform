## Purpose

Define how the FHIR `_lastUpdated` search parameter is evaluated server-side by
the SQL backend, enabling incremental synchronization queries that push only
changed records into a downstream FHIR repository without fetching and
post-filtering the full dataset.

## Requirements

### Requirement: Server-Side `_lastUpdated` Enforcement

When a FHIR query includes a `_lastUpdated` parameter, the SQL backend SHALL
translate it into a SQL `WHERE` clause predicate on the configured timestamp
column. Post-filtering in Java after full dataset retrieval is NOT acceptable.

#### Scenario: `_lastUpdated` applied to immunization retrieval

WHEN a query includes `_lastUpdated=ge2024-01-01`  
THEN the SQL immunization query includes `WHERE timestamp_column >= '2024-01-01'`  
AND no immunization rows outside that range are returned or processed

#### Scenario: `_lastUpdated` applied to patient candidate query

WHEN a query includes `_lastUpdated=ge2024-01-01`  
THEN the SQL patient candidate query includes `WHERE timestamp_column >= '2024-01-01'`  
AND no patient rows outside that range are fetched for IDIMatch scoring

#### Scenario: `_lastUpdated` absent — no filtering

WHEN a query does not include a `_lastUpdated` parameter  
THEN no timestamp predicate is added to either SQL query  
AND all records within the other search criteria are returned

---

### Requirement: FHIR Date Prefix Syntax

The `_lastUpdated` parameter SHALL support the full FHIR date prefix syntax:
`ge` (≥), `le` (≤), `gt` (>), `lt` (<). A single request may supply both a
lower and upper bound to express a closed date range. HAPI FHIR's
`DateRangeParam` is used to parse and carry this value through the call chain.

#### Scenario: Open-ended lower bound

WHEN `_lastUpdated=ge2024-01-01`  
THEN the WHERE clause predicate is `timestamp_column >= '2024-01-01'`

#### Scenario: Closed date range

WHEN the request supplies `_lastUpdated=ge2024-01-01` and `_lastUpdated=le2024-12-31`  
THEN the WHERE clause predicate is `timestamp_column >= '2024-01-01' AND timestamp_column <= '2024-12-31'`

---

### Requirement: Timestamp Column Declaration

The mapping configuration SHALL identify, for each resource type, which database
column provides the `_lastUpdated` value. This is declared by setting
`is_last_updated: true` on the relevant column mapping entry. That same column
also maps to `meta.lastUpdated` in the FHIR output so callers receive a
standards-conformant response.

#### Scenario: Timestamp column declared in mapping config

WHEN a column entry specifies `is_last_updated: true`  
THEN that column is used as the WHERE clause predicate target for `_lastUpdated`
filtering on its resource type  
AND the column value is written to `meta.lastUpdated` on the produced FHIR resource

#### Scenario: No timestamp column declared — `_lastUpdated` unsupported

WHEN no column entry for a resource type sets `is_last_updated: true`  
AND a `_lastUpdated` parameter is present in the query  
THEN the backend returns an `OperationOutcome` indicating `_lastUpdated` filtering
is not supported for that resource type in this configuration

---

### Requirement: Alignment with Bulk FHIR `_since`

The `_since` parameter on `$export` kick-off and the `_lastUpdated` parameter on
single-patient and population queries SHALL reference the same timestamp column
declared in the mapping configuration. This ensures that incremental delta syncs
via `$export` and targeted queries filtered by date return consistent results from
the same underlying data.

#### Scenario: Consistent results across both query paths

WHEN the same `is_last_updated` column is used for both single-patient queries
and bulk export  
THEN a record returned by a `_lastUpdated=ge2024-01-01` single-patient query
is also present in a `_since=2024-01-01` bulk export, and vice versa
