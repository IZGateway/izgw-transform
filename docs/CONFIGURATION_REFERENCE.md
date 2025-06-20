&larr;[Back to README](../README.md)

---

# Transformation Service Configuration Reference

This document aims to provide complete details about the configuration of the Transformation Service.

Configuration of the Transformation Service can be broken down into two areas:

1. Runtime Configuration &rarr; Details on how the application should _execute_. What port should it listen on? Which
   IZG Hub should it connect to? Where are the key and trust stores to use?
2. Application Configuration &rarr; Details on how the application _operates_. What Pipelines exist? Which Solutions are
   available?

## Runtime Configuration

The following table lists out the different possible configuration options for the Transformation Service. Each are
shown as environment variables and corresponding spring configuration properties. If there is a default value that is
listed.

Each section header will link to later in this document where descriptions for each item are located.

| Environment Variable                                                        | Spring Variable                                    | Default Value                                            |
|-----------------------------------------------------------------------------|----------------------------------------------------|----------------------------------------------------------|
| **[Application Configuration](#application-configuration)**                 |                                                    |                                                          |
| XFORM_SERVER_HOSTNAME                                                       | server.hostname                                    | dev.izgateway.org                                        | 
| XFORM_SERVER_PORT                                                           | server.port                                        | 444                                                      |
| XFORM_ALLOW_DELETE_VIA_API                                                  | xform.allow-delete-via-api                         | false                                                    | |
| **[SSL and Keystore Configuration](#ssl-and-keystore-configuration)**       |                                                    |                                                          |
| SSL_SHARE                                                                   | security.ssl-path                                  |                                                          |
| COMMON_PASS                                                                 | N/A                                                |                                                          |
| XFORM_CRYPTO_STORE_KEY_TOMCAT_SERVER_FILE                                   | server.ssl.key-store                               | Value of SSL_SHARE + /awsdev_keystore.bcfks              |
| XFORM_CRYPTO_STORE_KEY_WS_CLIENT_FILE                                       | client.ssl.key-store                               | Value of SSL_SHARE + /awsdev_keystore.bcfks              |
| XFORM_CRYPTO_STORE_TRUST_TOMCAT_SERVER_FILE                                 | server.ssl.trust-store                             | Value of SSL_SHARE + /awsdev_keystore.bcfks              |
| XFORM_CRYPTO_STORE_TRUST_WS_CLIENT_FILE                                     | client.ssl.trust-store                             | Value of SSL_SHARE + izgw_client_trust.bcfks             |
| **[JWT Configuration](#jwt-configuration)**                                 |                                                    |                                                          |
| XFORM_JWT_PROVIDER                                                          | jwt.provider                                       | shared-secret                                            |
| XFORM_JWT_SECRET                                                            | jwt.shared-secret                                  |                                                          |
| XFORM_JWT_URI                                                               | jwt.jwk-set-uri                                    |                                                          |
| XFORM_JWT_ROLES_CLAIM                                                       | jwt.roles-claim                                    | roles                                                    |
| XFORM_JWT_SCOPES_CLAIM                                                      | jwt.scopes-claim                                   | scope                                                    |
| **[Security Filter Configuration](#security-filter-configuration)**         |                                                    |                                                          |
| HUB_SECURITY_SECRET_HEADER_FILTER_ENABLED                                   | hub.security.secret-header-filter.enabled          | false                                                    |
| HUB_SECURITY_SECRET_HEADER_FILTER_KEY                                       | hub.security.secret-header-filter.key              |                                                          |
| HUB_SECURITY_SECRET_HEADER_FILTER_VALUE                                     | hub.security.secret-header-filter.value            |                                                          |
| HUB_SECURITY_SECRET_HEADER_FILTER_BYPASS_PATHS                              | hub.security.secret-header-filter.bypass-paths     | /rest/health                                             |
| HUB_SECURITY_IP_FILTER_ENABLED                                              | hub.security.ip-filter.enabled                     | false                                                    |
| HUB_SECURITY_IP_FILTER_ALLOWED_CIDR                                         | hub.security.ip-filter.allowed-cidr                |                                                          |
| **[Destination Configuration](#destination-configuration)**                 |                                                    |                                                          |
| XFORM_HUB_DESTINATION_ID                                                    | xform.destination.hub.id                           | hub                                                      |
| XFORM_HUB_DESTINATION                                                       | xform.destination.hub.uri                          | https://localhost/IISHubService                          |
| XFORM_DESTINATION_HUB_TYPE                                                  | xform.destination.hub.type                         | 5                                                        |
| XFORM_IIS_DESTINATION_ID                                                    | xform.destination.iis.id                           | iis                                                      |
| XFORM_IIS_DESTINATION                                                       | xform.destination.iis.uri                          | https://localhost/dev/IISService                         |
| XFORM_DESTINATION_IIS_TYPE                                                  | xform.destination.iis.type                         | 5                                                        |
| **[HL7 v2 to FHIR Configuration](#hl7-v2-to-fhir-configuration)**           |                                                    |                                                          |
| SENDING_APPLICATION                                                         | v2tofhir.sendingApplication                        | FHIR                                                     | |
| SENDING_FACILITY                                                            | v2tofhir.sendingFacility                           | DEV                                                      | |
| RECEIVING_APPLICATION                                                       | v2tofhir.receivingApplication                      | TEST                                                     | |
| RECEIVING_FACILITY                                                          | v2tofhir.receivingFacility                         | MOCK                                                     | |
| FACILITY_ID                                                                 | v2tofhir.facilityId                                | IZG                                                      | |
| **[Application Configuration Storage](#application_configuration_storage)** |                                                    |                                                          |
| SPRING_DATABASE                                                             | spring.database                                    | file                                                     |
| AMAZON_DYNAMODB_ENDPOINT                                                    | amazon.dynamodb.endpoint                           |                                                          |
| AMAZON_DYNAMODB_TABLE                                                       | amazon.dynamodb.table                              | izgw-hub                                                 |
| XFORM_CONFIGURATIONS_ORGANIZATIONS                                          | xform.configurations.organizations                 | testing/configuration/organizations.json                 |
| XFORM_CONFIGURATIONS_PIPELINES                                              | xform.configurations.pipelines                     | testing/configuration/pipelines.json                     |
| XFORM_CONFIGURATIONS_SOLUTIONS                                              | xform.configurations.solutions                     | testing/configuration/solutions.json                     |
| XFORM_CONFIGURATIONS_MAPPINGS                                               | xform.configurations.mappings                      | testing/configuration/mappings.json                      |
| XFORM_CONFIGURATIONS_ACCESS_CONTROL                                         | xform.configurations.access-control                | testing/configuration/access-control.json                |
| XFORM_CONFIGURATIONS_OPERATION_PRECONDITION_FIELDS                          | xform.configurations.operation-precondition-fields | testing/configuration/operation-precondition-fields.json |
| XFORM_CONFIGURATIONS_USERS                                                  | xform.configurations.users                         | testing/configuration/users.json                         |
| XFORM_CONFIGURATIONS_GROUP_ROLE_MAPPING                                     | xform.configurations.group-role-mapping            | testing/configuration/group-role-mapping.json            |
| **[Logging Configuration](#logging-configuration)**                         |                                                    |                                                          |
| LOGGING_LEVEL                                                               | N/A                                                | INFO                                                     |
| ELASTIC_API_KEY                                                             | N/A                                                |                                                          |
| ELASTIC_ENV_TAG                                                             | N/A                                                | dev                                                      |
| ELASTIC_HOST                                                                | N/A                                                |                                                          |
| ELASTIC_INDEX                                                               | N/A                                                | izgw-xform-service-dev                                   |

### Application Configuration

#### XFORM_SERVER_HOSTNAME

The hostname name that the Transformation Service will bind to and identify itself as when starting the embedded web
server.

#### XFORM_SERVER_PORT

The HTTPS port that the Transformation Service will use.

#### XFORM_ALLOW_DELETE_VIA_API

Controls whether DELETE operations are permitted via REST API endpoints; when false, DELETE requests return HTTP 403
Forbidden. Valid values are true or false.

Possibly will be removed, see IGDD-2076.

### SSL and Keystore Configuration

#### SSL_SHARE

Base directory path where BCFKS keystore files are located, used as a prefix for other SSL file paths.

If this is set, for example, to ```/conf/ssl``` and the _XFORM_CRYPTO\_*_ settings are **NOT** set then the default
names for the keystores will be used as described below.

#### COMMON_PASS

The password necessary to open the keystore files. Until further notice, both the server and client keystore files must
use the same password.

#### XFORM_CRYPTO_STORE_KEY_TOMCAT_SERVER_FILE

Full path to the keystore file containing the server's SSL private key and certificate for HTTPS connections.

This needs to be the full path to the file, if this value is specified, SSL_SHARE is not taken into account.

Example: ```/usr/share/izgw-xform/conf/server_keystore.bcfks```

TODO - This is for INBOUND connections correct?

#### XFORM_CRYPTO_STORE_KEY_WS_CLIENT_FILE

Full path to the keystore file containing the client's SSL private key and certificate for outbound HTTPS calls.

This would contain the certificate that Transformation Service will present for outbound connections requiring mTLS.

This needs to be the full path to the file, if this value is specified, SSL_SHARE is not taken into account.

Example: ```/usr/share/izgw-xform/conf/server_keystore.bcfks```

#### XFORM_CRYPTO_STORE_TRUST_TOMCAT_SERVER_FILE

Full path to the truststore file containing trusted CA certificates for validating incoming client certificates.

This needs to be the full path to the file, if this value is specified, SSL_SHARE is not taken into account.

Example: ```/usr/share/izgw-xform/conf/server_keystore.bcfks```

#### XFORM_CRYPTO_STORE_TRUST_WS_CLIENT_FILE

Full path to the truststore file containing trusted CA certificates for validating server certificates on outbound
connections.

The Transformation Service will fail to connect to any outbound server whose certificate is not trusted via this file.

This needs to be the full path to the file, if this value is specified, SSL_SHARE is not taken into account.

Example: ```/usr/share/izgw-xform/conf/client_keystore.bcfks```

### JWT Configuration

#### XFORM_JWT_PROVIDER

This setting tells Transformation Service how to validate received JWT tokens.

There are two possible values for this:

- shared-secret &rarr; JWT tokens will be HMAC SHA-256 signed with a specified shared secret
    - This is the default value if not specified
    - XFORM_JWT_SECRET configuration is required for this option
- jwks &rarr; JWT tokens will be validated using a specified JWKS endpoint
    - Used with an IDP such as Okta or Keycloak
    - XFORM_JWT_URI configuration is required for this option

#### XFORM_JWT_SECRET

This is the shared secret used to validate JWT tokens if XFORM_JWT_PROVIDER is configured as shared-secret.

#### XFORM_JWT_URI

This is the JWKS endpoint necessary to validate a JWT token when XFORM_JWT_PROVIDER is configured as jwks.

**Please Note** that the certificate presented by the endpoint will need to be in the Transformation Service Client
Trust Store. See [Transformation Service SSL/Keystore File Reference](./docs/KEYSTORE_FILES.md) for information about
the keystore files.

#### XFORM_JWT_ROLES_CLAIM

This lets the Transformation Service know which claim in the JWT would have the user's roles. By default, this is
```roles```.

Transformation Service treats this as a _Group_ which can be mapped to a series of roles which will define what the user
can do in the system. This is controlled via the GroupRoleMapping configuration.

#### XFORM_JWT_SCOPES_CLAIM

This lets the Transformation Service know which claim in the JWT would have the user's scopes. By default, this is
```scope```.

### Security Filter Configuration

#### HUB_SECURITY_SECRET_HEADER_FILTER_ENABLED

Enable secret HTTP header validation.

#### HUB_SECURITY_SECRET_HEADER_FILTER_KEY

Secret header key name.

#### HUB_SECURITY_SECRET_HEADER_FILTER_VALUE

Expected secret header value.

#### HUB_SECURITY_SECRET_HEADER_FILTER_BYPASS_PATHS

Transformation Service web-service paths that are allowed to bypass the secret header filter.

#### HUB_SECURITY_IP_FILTER_ENABLED

Enables IP address filtering. Any connections from an IP address not configured is rejected.

#### HUB_SECURITY_IP_FILTER_ALLOWED_CIDR

Comma-separated CIDR blocks for allowed IPs. IPv4 and IPv6 are allowed.

Example: 10.9.90.0/24, 10.9.95.0/24, 127.0.0.1/32, ::1/128

### Destination Configuration

These options tell the Transformation Service about the IZG Hub instance that it is connecting to.

#### XFORM_HUB_DESTINATION

The location of the IISHubService endpoint for IZG Hub.

Example: https://dev.izgateway.org/IISHubService

#### XFORM_HUB_DESTINATION_ID

The destination id to use when connecting to the IISHubService endpoint for IZG Hub.

#### XFORM_DESTINATION_HUB_TYPE

The destination type for IISHubService endpoint in IZG Hub that Transformation Service will be connecting to.

#### XFORM_IIS_DESTINATION

The location of the IISService endpoint for IZG Hub.

Example: https://dev.izgateway.org/dev/IISService

#### XFORM_IIS_DESTINATION_ID

The destination id to use when connecting to the IISService endpoint for IZG Hub.

#### XFORM_DESTINATION_IIS_TYPE

The destination type for IISService endpoint in IZG Hub that Transformation Service will be connecting to.

### HL7 v2 to FHIR Configuration

#### SENDING_APPLICATION

When Transformation Service converts a FHIR message to HL7v2, this value will be placed in in the MSH.3 field in the
generated HL7v2 message.

#### SENDING_FACILITY

When Transformation Service converts a FHIR message to HL7v2, this value will be placed in in the MSH.4 field in the
generated HL7v2 message.

#### RECEIVING_APPLICATION

When Transformation Service converts a FHIR message to HL7v2, this value will be placed in in the MSH.5 field in the
generated HL7v2 message.

#### RECEIVING_FACILITY

When Transformation Service converts a FHIR message to HL7v2, this value will be placed in in the MSH.6 field in the
generated HL7v2 message.

#### FACILITY_ID

When the Transformation Service converts a FHIR message to HL7v2, this value is used to set the facility id in the
message sent to IZG Hub.

### Application Configuration Storage

These are the options involved with configuring the storage of the Application Configuration necessary for the
Transformation Service to run:

- SPRING_DATABASE
- AMAZON_DYNAMODB_ENDPOINT
- AMAZON_DYNAMODB_TABLE
- XFORM_CONFIGURATIONS_DIRECTORY
- XFORM_CONFIGURATIONS_ORGANIZATIONS
- XFORM_CONFIGURATIONS_PIPELINES
- XFORM_CONFIGURATIONS_SOLUTIONS
- XFORM_CONFIGURATIONS_MAPPINGS
- XFORM_CONFIGURATIONS_ACCESS_CONTROL
- XFORM_CONFIGURATIONS_OPERATION_PRECONDITION_FIELDS
- XFORM_CONFIGURATIONS_USERS
- XFORM_CONFIGURATIONS_GROUP_ROLE_MAPPING

The configuration of application configuration storage is covered in detail
here: [Application Configuration Storage](./APPLICATION_CONFIGURATION_STORAGE.md)

### Logging Configuration

#### LOGGING_LEVEL

Allows you to adjust the verbosity of the logs output by the Transformation Service. The application uses SLF4J,
possible values for logging level can be viewed here: https://www.slf4j.org/api/org/apache/log4j/Level.html

#### Elastic Settings

The Transformation Service, while running in a Docker container, has Elastic Stack integration that collects both logs
and metrics and ships them to Elasticsearch Cloud.

#### ELASTIC_API_KEY

Elastic Cloud API key necessary to be able to push data.

#### ELASTIC_ENV_TAG

Tag identifying the environment in Elastic Cloud. Possible values for Audacious Inquiry use:

- unknown
- prod
- test
- onboard
- staging
- dev

#### ELASTIC_HOST

The full endpoint for Elastic cloud that you want to push data to.

#### ELASTIC_INDEX

The name of the index used in Elastic Cloud for this instance.

---

&larr;[Back to README](../README.md)


## Application Configuration

There are different entities in the Transformation Service system necessary for it to operate:

- Access Controls
- Group Role Mappings
- Mappings
- Operation / Precondition Fields
- Organizations
- Pipelines
- Solutions
- Users

### Access Controls

Access Controls are used to determine what rights a caller to the system has, when only a certificate is presented (So no JWT).

In the scenario when a JWT is not presented, the common name of the certificate is used to find a User. That User must have an Access Control setup.

There is a 1:1 relationship between Access Controls and Users.

Each Access Control can have one or more _roles_, which will need to represent roles specified in [Roles.java](../src/main/java/gov/cdc/izgateway/xform/security/Roles.java).

Example file: [access-control.json](../testing/configuration/access-control.json)

### Group Role Mappings

The Group to Role Mappings exist to determine what tasks a User, authenticated via JWT token, has the ability to perform in the Transformation Service.

For example, your configured IDP generates JWT tokens like:

```json
{
  "header": {
    "kid": "M3pRwVnKx8qY2DbF7LmN9CtPsHgJzWEeRKLQV6TxBnA",
    "alg": "RS256"
  },
  "payload": {
    "ver": 1,
    "jti": "AT.mX9vQpL7N2kFRt8s_H-wYcz4xerBj-ghPdM1GfzEykj",
    "iss": "https://dev-example.oktapreview.com/oauth2/default",
    "aud": "sample application",
    "iat": 1750440000,
    "exp": 1750443600,
    "cid": "0oabc123def456ghi789",
    "uid": "00udef456ghi789jkl012",
    "scp": [
      "openid"
    ],
    "auth_time": 1750439998,
    "sub": "jdoe@example.com",
    "Groups": [
      "Xform Solutions Engineer"
    ]
  },
  "signature": "AbC1DeFgHiJkLmNoPqRsTuVwXyZ2aBcDeFgHiJkLmNoPqRsTuVwXyZ3bCdEfGhIjKlMnOpQrStUvWxYz4AbCdEfGhIjKlMnOpQrStUvWxYz5cDeFgHiJkLmNoPqRsTuVwXyZ6dEfGhIjKlMnOpQrStUvWxYz7eFgHiJkLmNoPqRsTuVwXyZ8fGhIjKlMnOpQrStUvWxYz9gHiJkLmNoPqRsTuVwXyZaBcDeFgHiJkLmNoPqRsTuVw"
}
```

You'd have XFORM_JWT_ROLES_CLAIM configured to _Groups_ so that the Transformation Service would know where to find the list of groups that this user belongs to.

Transformation Service would then look for a Group to Role Mapping where groupName is _Xform Solutions Engineer_.  An example from our testing configuration:

```json
{
  "id": "a3c729dd-4f0e-48c7-a6a6-18c66f8cba93",
  "groupName": "Xform Solutions Engineer",
  "active": true,
  "roles": [
    "organization-reader",
    "pipeline-deleter",
    "pipeline-reader",
    "pipeline-writer",
    "solution-deleter",
    "solution-reader",
    "solution-writer"
  ]
}
```

The User would be given the Roles listed in the above example. Similar to Access Control, the roles need to represent roles specified in [Roles.java](../src/main/java/gov/cdc/izgateway/xform/security/Roles.java).

Example file: [group-role-mapping.json](../testing/configuration/group-role-mapping.json)

### Mappings

Mappings are used specifically by the Mapper Operation in the Transformation Service.

These can be thought of as entries in a _lookup table_ to provide a From &rarr; To mapping for data being manipulated by the Transformation Service.

Each Mapping entry is tied to an Organization so that each Organization can maintain their set of mappings distinct from other Organizations.

An example Mapping configuration entry:

```json
{
  "id": "8f2e841e-1cc1-4ce6-a1dd-7a3feafe9e16",
  "active": true,
  "organizationId": "11490ced-151d-48f4-b33b-b9f19bb24ac7",
  "codeSystem": "HOSPA",
  "code": "W",
  "targetCodeSystem": "CDCREC",
  "targetCode": "2106-3"
}
```

This would be used to map an internal Race code for White to a different code set.

Example file: [mappings.json](../testing/configuration/mappings.json)

### Operation / Precondition Fields

This configuration exists to provide a suggested list of _fields_ available to configure in Preconditions and Operations. 

Each entry will tell if the field is intended to be used for Preconditions, Operations, or both. The dataPath field contains the data intended to be inserted into a Precondition or Operation configuration.

Please **note** that at this time there is no restriction that would prevent you from configuring an Operation or Precondition with a field not stored in the system. This configuration was initially added for a UI to use. This may change in the future, documentation will be updated to reflect.

Examples:

This 

```json
{
  "id": "b8f6adcb-ffa4-4793-a157-ffc2db85da7b",
  "fieldName": "Sending Application",
  "dataPath": "/MSH-3-1",
  "forPrecondition": true,
  "forOperation": false,
  "active": true
}
```

```json
{
  "id": "07d52845-6c34-4318-a8c9-2117072ca6e3",
  "fieldName": "Facility ID",
  "dataPath": "context.FacilityID",
  "forPrecondition": true,
  "forOperation": false,
  "active": true
}
```

Example file: [operation-precondition-fields.json](../testing/configuration/operation-precondition-fields.json)

### Organizations

Organizations are one of the most important objects in the Tranformation Service. An Organization might be an IIS Jurisdiction or a Vendor of IIS software. 

Each User can be associated with one or more Organizations.

Each Pipeline and Mapping is associated with a specific Organization. Meaning that each Pipeline is distinct for an Organization.

An Organization can be associated with a certificate common name. This is used when a message is received into the Transformation Service, to look up the Organization that is sending the data. That Organization is then used as a piece of information to determine the Pipeline to use. 

Examples: 

```json
{
  "organizationName": "Lunar Colony Health Alliance",
  "id": "d339cd15-2e57-4456-94b6-1e14f079a0de",
  "active": true,
  "commonName": ""
}
```

```json
{
  "organizationName": "Nova Prime Medical Research",
  "id": "5415ead7-17b2-48b0-baa9-679ec85e05cd",
  "active": true,
  "commonName": "example.org.izgateway.org"
}
```


Example file: [organizations.json](../testing/configuration/organizations.json)

### Pipelines

A Pipeline details how a message will be _transformed_ by the system.

Each Pipeline is associated with exactly one Organization. And it is further associated with an inbound endpoint and outbound endpoint. Meaning there is one Pipeline for each combination of Organization, Inbound Endpoint, and Outbound Endpoint.

So an Organization can have one Pipeline inbound for the IISHubService endpoint sending outbound to the IISHubService endpoint at IZ Gateway Hub and then another Pipeline for inbound IIService outbound to IISService at IZ Gateway Hub.

Each Pipeline is made up of one or more _"pipes"_ which are Solutions which have been configured in the system. Furthermore, each pipe can have a set of preconditions configured to determine if the pipe should execute.

Example: 

```json
{
  "pipelineName": "Example Pipeline",
  "id": "a4c93f34-cc44-45a8-b500-6d5f2a55c8d8",
  "organizationId": "76cadebf-d407-4114-80d9-d9bc94f6f116",
  "description": "Pipeline for documentation example...",
  "inboundEndpoint": "izgts:IISHubService",
  "outboundEndpoint": "izghub:IISHubService",
  "active": true,
  "pipes": [
    {
      "id": "8b837e9a-69b7-4b65-a63b-2f2acca0bc9f",
      "solutionId": "2a4b46c2-f8e6-4ba5-b2b3-f9950f2f4216",
      "solutionVersion": "1.0",
      "preconditions": [
        {
          "method": "equals",
          "id": "242d93bd-96c4-47cc-8bea-9c3a9ead8d90",
          "dataPath": "/MSH-9-1",
          "comparisonValue": "VXU"
        }
      ]
    },
    {
      "id": "4b0c3f52-c168-4e4d-b068-1ddbd7060d10",
      "solutionId": "e326caeb-4272-4d0f-81a6-1e8340b038b3",
      "solutionVersion": "1.0",
      "preconditions": []
    }
  ]
}
```

This example shows a Pipeline for Organization with id 76cadebf-d407-4114-80d9-d9bc94f6f116 for inbound endpoint izgts:IISHubService and outbound endpoint izghub:IISHubService.

The pipeline will execute two pipes, one for Solution with id 2a4b46c2-f8e6-4ba5-b2b3-f9950f2f4216 and another for Solution with id e326caeb-4272-4d0f-81a6-1e8340b038b3. 

Of note, the pipe specifies not only the Solution by id but also version. This is because Solutions as they evolve will receive a new version. Each pipe is therefore locked in by id and version so that updates to Solutions do not automatically affect Pipelines without testing.

Also, you will see that the first pipe has a precondition. That pipe will only execute if the HL7v2 Message Type (MSH.9.1) is VXU. A pipe can have multiple Preconditions specified, in which case _all_ the Preconditions must evaluate to true.

Example file: [pipelines.json](../testing/configuration/pipelines.json)

### Solutions

A Solution represents a specific set of Operations intended to _transform_ a message coming through the system. A Solution is intended to represent a specific change to a message.  For example, Zip Fixer could contain a set of Operations which make sure that all zip codes in a HL7v2 VXU message are in a standardized format.

Important pieces of a Solution:

- solutionName &rarr; A descriptive name of what the Solution is to be used for. There is also a description field available.
- version &rarr; A version for this Solution. For example, you could have a Zip Fixer Solution that you need to update. The update would be saved with version 2.0.
- requestOperations &rarr; These are Operations that the Solution will execute on the _request_ message or the message as received inbound to the Transformation Service by the caller.
- responseOperations &rarr; These are Operations that the Solution will execute on the _response_ message before returning to the original caller. This would be the response from the downstream system.

Simple example flow:
- A QBP message from the caller goes to IZ Gateway Hub. This QBP is the _request_ message which will have changes made by the Solution's requestOperations.
- IZ Gateway Hub response with a RSP message. This RSP is the _response_ message which will have changes made by the Solution's responseOperations.

Example file: [solutions.json](../testing/configuration/solutions.json)

### Users

Identifies users who can interact with the Transformation Service. Their level of interaction is determined by either Access Controls or Group Role Mappings.

When a JWT is supplied, the _subject_ claim is used as the user's name. This value is matched to the _userName_ of a User object in the system to locate a configured user.

When no JWT is supplied, the common name of the presented certificate is matched to the _userName_ of a User object.

Example file: [users.json](../testing/configuration/users.json)
