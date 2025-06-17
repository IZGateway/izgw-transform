&larr;[Back to README](../README.md)

---

# Transformation Service Configuration Reference

This document ains to provide complete details about the configuration of the Transformation Service.

Configuration of the Transformation Service can be broken down into two areas:

1. Runtime Configuration &rarr; Details on how the application should _execute_. What port should it listen on? Which
   IZG Hub should it connect to? Where are the key and trust stores to use?
2. Business Logic Configuration &rarr; Details on how the application _operates_. What Pipelines exist? Which Solutions
   are available?

## Runtime Configuration

The following table lists out the different possible configuration options for the Transformation Service. Each are shown as environment variables and corresponding spring configuration properties. If there is a default value that is listed.

Each section header will link to later in this document where descriptions for each item are located.

| Environment Variable                                                                        | Spring Variable                                    | Default Value                                            |
|---------------------------------------------------------------------------------------------|----------------------------------------------------|----------------------------------------------------------|
| **[Application Configuration](#application-configuration)**                                 |                                                    |                                                          |
| XFORM_SERVER_HOSTNAME                                                                       | server.hostname                                    | dev.izgateway.org                                        | 
| XFORM_SERVER_PORT                                                                           | server.port                                        | 444                                                      |
| XFORM_ALLOW_DELETE_VIA_API                                                                  | xform.allow-delete-via-api                         | false                                                    | |
| **[SSL and Keystore Configuration](#ssl-and-keystore-configuration)**                       |                                                    |                                                          |
| SSL_SHARE                                                                                   | security.ssl-path                                  |                                                          |
| COMMON_PASS                                                                                 | N/A                                                |                                                          |
| XFORM_CRYPTO_STORE_KEY_TOMCAT_SERVER_FILE                                                   | server.ssl.key-store                               | Value of SSL_SHARE + /awsdev_keystore.bcfks              |
| XFORM_CRYPTO_STORE_KEY_WS_CLIENT_FILE                                                       | client.ssl.key-store                               | Value of SSL_SHARE + /awsdev_keystore.bcfks              |
| XFORM_CRYPTO_STORE_TRUST_TOMCAT_SERVER_FILE                                                 | server.ssl.trust-store                             | Value of SSL_SHARE + /awsdev_keystore.bcfks              |
| XFORM_CRYPTO_STORE_TRUST_WS_CLIENT_FILE                                                     | client.ssl.trust-store                             | Value of SSL_SHARE + izgw_client_trust.bcfks             |
| **[JWT Configuration](#jwt-configuration)**                                                 |                                                    |                                                          |
| XFORM_JWT_PROVIDER                                                                          | jwt.provider                                       | shared-secret                                            |
| XFORM_JWT_SECRET                                                                            | jwt.shared-secret                                  |                                                          |
| XFORM_JWT_URI                                                                               | jwt.jwk-set-uri                                    |                                                          |
| XFORM_JWT_ROLES_CLAIM                                                                       | jwt.roles-claim                                    | roles                                                    |
| XFORM_JWT_SCOPES_CLAIM                                                                      | jwt.scopes-claim                                   | scope                                                    |
| **[Destination Configuration](#destination-configuration)**                                 |                                                    |                                                          |
| XFORM_HUB_DESTINATION_ID                                                                    | xform.destination.hub.id                           | hub                                                      |
| XFORM_HUB_DESTINATION                                                                       | xform.destination.hub.uri                          | https://localhost/IISHubService                          |
| XFORM_DESTINATION_HUB_TYPE                                                                  | xform.destination.hub.type                         | 5                                                        |
| XFORM_IIS_DESTINATION_ID                                                                    | xform.destination.iis.id                           | iis                                                      |
| XFORM_IIS_DESTINATION                                                                       | xform.destination.iis.uri                          | https://localhost/dev/IISService                         |
| XFORM_DESTINATION_IIS_TYPE                                                                  | xform.destination.iis.type                         | 5                                                        |
| **[HL7 v2 to FHIR Configuration](#hl7-v2-to-fhir-configuration)**                           |                                                    |                                                          |
| SENDING_APPLICATION                                                                         | v2tofhir.sendingApplication                        | FHIR                                                     | |
| SENDING_FACILITY                                                                            | v2tofhir.sendingFacility                           | DEV                                                      | |
| RECEIVING_APPLICATION                                                                       | v2tofhir.receivingApplication                      | TEST                                                     | |
| RECEIVING_FACILITY                                                                          | v2tofhir.receivingFacility                         | MOCK                                                     | |
| FACILITY_ID                                                                                 | v2tofhir.facilityId                                | IZG                                                      | |
| **[Business Logic Data Storage Configuration](#business-logic-data-storage-configuration)** |                                                    |                                                          |
| SPRING_DATABASE                                                                             | spring.database                                    | file                                                     |
| AMAZON_DYNAMODB_ENDPOINT                                                                    | amazon.dynamodb.endpoint                           |                                                          |
| AMAZON_DYNAMODB_TABLE                                                                       | amazon.dynamodb.table                              | izgw-hub                                                 |
| XFORM_CONFIGURATIONS_ORGANIZATIONS                                                          | xform.configurations.organizations                 | testing/configuration/organizations.json                 |
| XFORM_CONFIGURATIONS_PIPELINES                                                              | xform.configurations.pipelines                     | testing/configuration/pipelines.json                     |
| XFORM_CONFIGURATIONS_SOLUTIONS                                                              | xform.configurations.solutions                     | testing/configuration/solutions.json                     |
| XFORM_CONFIGURATIONS_MAPPINGS                                                               | xform.configurations.mappings                      | testing/configuration/mappings.json                      |
| XFORM_CONFIGURATIONS_ACCESS_CONTROL                                                         | xform.configurations.access-control                | testing/configuration/access-control.json                |
| XFORM_CONFIGURATIONS_OPERATION_PRECONDITION_FIELDS                                          | xform.configurations.operation-precondition-fields | testing/configuration/operation-precondition-fields.json |
| XFORM_CONFIGURATIONS_USERS                                                                  | xform.configurations.users                         | testing/configuration/users.json                         |
| XFORM_CONFIGURATIONS_GROUP_ROLE_MAPPING                                                     | xform.configurations.group-role-mapping            | testing/configuration/group-role-mapping.json            |
| **[Logging Configuration](#logging-configuration)**                                         |                                                    |                                                          |
| LOGGING_LEVEL                                                                               | N/A                                                |                                                          |
| ELASTIC_API_KEY                                                                             | N/A                                                |                                                          |
| ELASTIC_ENV_TAG                                                                             | N/A                                                | dev                                                      |
| ELASTIC_HOST                                                                                | N/A                                                |                                                          |
| ELASTIC_INDEX                                                                               | N/A                                                | izgw-xform-service-dev                                   |

### Application Configuration

#### XFORM_SERVER_HOSTNAME

The hostname name that the Transformation Service will bind to and identify itself as when starting the embedded web server.

#### XFORM_SERVER_PORT

The HTTPS port that the Transformation Service will use.

#### XFORM_ALLOW_DELETE_VIA_API

Controls whether DELETE operations are permitted via REST API endpoints; when false, DELETE requests return HTTP 403 Forbidden. Valid values are true or false.

Possibly will be removed, see IGDD-2076.

### SSL and Keystore Configuration

#### SSL_SHARE

Base directory path where BCFKS keystore files are located, used as a prefix for other SSL file paths.

If this is set, for example, to ```/conf/ssl``` and the _XFORM_CRYPTO\_*_ settings are **NOT** set then the default names for the keystores will be used as described below.

#### COMMON_PASS

The password necessary to open the keystore files. Until further notice, both the server and client keystore files must use the same password.

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

Full path to the truststore file containing trusted CA certificates for validating server certificates on outbound connections.

The Transformation Service will fail to connect to any outbound server whose certificate is not trusted via this file.

This needs to be the full path to the file, if this value is specified, SSL_SHARE is not taken into account.

Example: ```/usr/share/izgw-xform/conf/client_keystore.bcfks```


### Business Logic Configuration

TODO

### JWT Configuration

TODO

### Destination Configuration

TODO

### HL7 v2 to FHIR Configuration

TODO

### Business Logic Data Storage Configuration

TODO

### Logging Configuration

TODO

# TODO & Follow-up

- IIS and HUB "Type" - need to document what this does
- FIX_NEWLINES - is never used in Transformation Service. Is read in Application but never set in SoapMessageWriter (
  hard-coded to true). Tech debt ticket: IGDD-2074
- Add tech debt ticket for XFORM_SERVER_HOSTNAME being dev.izgateway.org, should specify xform

---

&larr;[Back to README](../README.md)
