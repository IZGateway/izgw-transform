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

### Group Role Mappings
### Mappings
### Operation / Precondition Fields
### Organizations
### Pipelines
### Solutions
### Users

Identifies users who can interact with the Transformation Service. Their level of interaction is determined by either Access Controls or Group Role Mappings.

When a JWT is supplied, the _subject_ claim is used as the user's name. This value is matched to the _userName_ of a User object in the system to locate a configured user.

When no JWT is supplied, the common name of the presented certificate is matched to the _userName_ of a User object.
