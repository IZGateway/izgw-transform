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

| Environment Variable                               | Spring Variable                                    | Default Value                                            |
|----------------------------------------------------|----------------------------------------------------|----------------------------------------------------------|
| XFORM_CRYPTO_STORE_KEY_TOMCAT_SERVER_FILE          | server.ssl.key-store                               | Value of SSL_SHARE + /awsdev_keystore.bcfks              |
| XFORM_CRYPTO_STORE_KEY_WS_CLIENT_FILE              | client.ssl.key-store                               | Value of SSL_SHARE + /awsdev_keystore.bcfks              |
| XFORM_CRYPTO_STORE_TRUST_TOMCAT_SERVER_FILE        | server.ssl.trust-store                             | Value of SSL_SHARE + /awsdev_keystore.bcfks              |
| XFORM_CRYPTO_STORE_TRUST_WS_CLIENT_FILE            | client.ssl.trust-store                             | Value of SSL_SHARE + izgw_client_trust.bcfks             |
| COMMON_PASS                                        | N/A                                                |                                                          |
| LOGGING_LEVEL                                      | N/A                                                |                                                          |
| XFORM_ALLOW_DELETE_VIA_API                         | xform.allow-delete-via-api                         | false                                                    |
| XFORM_HUB_DESTINATION_ID                           | xform.destination.hub.id                           | hub                                                      |
| XFORM_HUB_DESTINATION                              | xform.destination.hub.uri                          | https://localhost/IISHubService                          |
| XFORM_DESTINATION_HUB_TYPE                         | xform.destination.hub.type                         | 5                                                        |
| XFORM_IIS_DESTINATION_ID                           | xform.destination.iis.id                           | iis                                                      |
| XFORM_IIS_DESTINATION                              | xform.destination.iis.uri                          | https://localhost/dev/IISService                         |
| XFORM_DESTINATION_IIS_TYPE                         | xform.destination.iis.type                         | 5                                                        |
| XFORM_CONFIGURATIONS_ORGANIZATIONS                 | xform.configurations.organizations                 | testing/configuration/organizations.json                 |
| XFORM_CONFIGURATIONS_PIPELINES                     | xform.configurations.pipelines                     | testing/configuration/pipelines.json                     |
| XFORM_CONFIGURATIONS_SOLUTIONS                     | xform.configurations.solutions                     | testing/configuration/solutions.json                     |
| XFORM_CONFIGURATIONS_MAPPINGS                      | xform.configurations.mappings                      | testing/configuration/mappings.json                      |
| XFORM_CONFIGURATIONS_ACCESS_CONTROL                | xform.configurations.access-control                | testing/configuration/access-control.json                |
| XFORM_CONFIGURATIONS_OPERATION_PRECONDITION_FIELDS | xform.configurations.operation-precondition-fields | testing/configuration/operation-precondition-fields.json |
| XFORM_CONFIGURATIONS_USERS                         | xform.configurations.users                         | testing/configuration/users.json                         |
| XFORM_CONFIGURATIONS_GROUP_ROLE_MAPPING            | xform.configurations.group-role-mapping            | testing/configuration/group-role-mapping.json            |
| XFORM_JWT_PROVIDER                                 | jwt.provider                                       | shared-secret                                            |
| XFORM_JWT_SECRET                                   | jwt.shared-secret                                  |                                                          |
| XFORM_JWT_URI                                      | jwt.jwk-set-uri                                    |                                                          |
| XFORM_JWT_ROLES_CLAIM                              | jwt.roles-claim                                    | roles                                                    |
| XFORM_JWT_SCOPES_CLAIM                             | jwt.scopes-claim                                   | scope                                                    |
| SPRING_DATABASE                                    | spring.database                                    | file                                                     |
| AMAZON_DYNAMODB_ENDPOINT                           | amazon.dynamodb.endpoint                           |                                                          |
| AMAZON_DYNAMODB_TABLE                              | amazon.dynamodb.table                              | izgw-hub                                                 |
| ELASTIC_API_KEY                                    | N/A                                                |                                                          |
| ELASTIC_ENV_TAG                                    | N/A                                                | dev                                                      |
| ELASTIC_HOST                                       | N/A                                                |                                                          |
| ELASTIC_INDEX                                      | N/A                                                | izgw-xform-service-dev                                   |
| SENDING_APPLICATION                                | v2tofhir.sendingApplication                        | FHIR                                                     |                                                    |
| SENDING_FACILITY                                   | v2tofhir.sendingFacility                           | DEV                                                      |                                                      |
| RECEIVING_APPLICATION                              | v2tofhir.receivingApplication                      | TEST                                                     |                                                    |
| RECEIVING_FACILITY                                 | v2tofhir.receivingFacility                         | MOCK                                                     |                                                     |
| FACILITY_ID                                        | v2tofhir.facilityId                                | IZG                                                      |                                                    |
| SSL_SHARE                                          | security.ssl-path                                  |                                                          |
| XFORM_SERVER_HOSTNAME                              | server.hostname                                    | dev.izgateway.org                                        | 
| XFORM_SERVER_PORT                                  | server.port                                        | 444                                                      | 

### TODO & Follow-up

- IIS and HUB "Type" - need to document what this does
- FIX_NEWLINES - is never used in Transformation Service. Is read in Application but never set in SoapMessageWriter (
  hard-coded to true). Tech debt ticket: IGDD-2074
- Add tech debt ticket for XFORM_SERVER_HOSTNAME being dev.izgateway.org, should specify xform

## Business Logic Configuration

TODO

---

&larr;[Back to README](../README.md)
