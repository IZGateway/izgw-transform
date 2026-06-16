## Purpose

Establish a formal Java interface contract between the Transformation Service
front-end (query intake) and back-end implementations (IZ Gateway, SQL connector)
so that neither side carries a compile-time dependency on the other.

## Requirements

### Requirement: IQueryRequest Interface

A `IQueryRequest` interface (or immutable POJO) SHALL encapsulate all data the
back-end needs to process a query: patient demographics, requested destination,
and any additional query parameters.

#### Scenario: Front-end constructs query request

WHEN the front-end receives an inbound query  
THEN it constructs an `IQueryRequest` instance populated from the inbound data  
AND passes it to the selected back-end connector without referencing any
connector-specific class

#### Scenario: SQL connector receives query request

WHEN the `SqlBackendConnector` receives an `IQueryRequest`  
THEN it extracts patient demographic fields using only the `IQueryRequest` interface methods  
AND does not cast or depend on the front-end's concrete request type

---

### Requirement: IBackendConnector Interface

A `IBackendConnector` interface SHALL define the contract all back-end
implementations must fulfill, including the existing IZ Gateway connector.

#### Scenario: IZ Gateway connector satisfies interface

WHEN the existing IZ Gateway connector is refactored to implement `IBackendConnector`  
THEN all existing callers continue to function without modification  
AND the refactor introduces no behavior changes

#### Scenario: SQL connector satisfies interface

WHEN `SqlBackendConnector` implements `IBackendConnector`  
THEN it can be substituted anywhere an `IBackendConnector` is expected

---

### Requirement: Destination-Based Connector Selection

A `ConnectorRouter` (or equivalent) SHALL select the appropriate `IBackendConnector`
implementation at runtime based on the destination field of the `IQueryRequest`.

#### Scenario: Destination "sql" routes to SQL connector

WHEN `IQueryRequest.getDestination()` returns `"sql"`  
THEN `ConnectorRouter` selects `SqlBackendConnector`

#### Scenario: Any other destination routes to IZ Gateway connector

WHEN `IQueryRequest.getDestination()` returns any value other than `"sql"`  
THEN `ConnectorRouter` selects the IZ Gateway connector (existing behavior)

---

### Requirement: Response Interface

A `IQueryResponse` interface SHALL encapsulate the FHIR Bundle (or error) returned
by any back-end connector, so the front-end can render responses without knowing
which connector produced them.

#### Scenario: Response rendered uniformly

WHEN either the IZ Gateway connector or the SQL connector returns an `IQueryResponse`  
THEN the front-end renders the response identically regardless of source
