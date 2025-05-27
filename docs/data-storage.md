# Transformation Service Data Storage

The purpose of this document is to explain how the Transformation Service stores the configuration necessary for its proper operation.

There are currently two storage options:

1. File-based (in JSON files)
2. AWS DynamoDB

There is also the opportunity to have the system migrate data from files to DynamoDB. Please note that this is a one way migration, you are able to migrate data from existing files to DynamoDB but not from DynamoDB back to files.

## Configuration

The type of storage is configured via the XFORM_REPOSITORY_TYPE environment variable. There are currently three options for this setting:

- file
  - This is the default is the environment variable is not set
- dynamodb
- migration

### File-based Storage Configuration

To use file-based storage, XFORM_REPOSITORY_TYPE will need to be set to ```file```.

In addition, each entity type's file location will need to be configured. These environment variables are:

- XFORM_CONFIGURATIONS_ORGANIZATIONS
- XFORM_CONFIGURATIONS_PIPELINES
- XFORM_CONFIGURATIONS_SOLUTIONS
- XFORM_CONFIGURATIONS_MAPPINGS
- XFORM_CONFIGURATIONS_ACCESS-CONTROL
- XFORM_CONFIGURATIONS_OPERATION-PRECONDITION-FIELDS
- XFORM_CONFIGURATIONS_USERS
- XFORM_CONFIGURATIONS_GROUP-ROLE-MAPPING

### AWS DynamoDB Storage Configuration

To use DynamoDB storage, XFORM_REPOSITORY_TYPE will need to be set to ```dynamodb```.

In addition, you may also configure the following items:

- XFORM_DYNAMODB_ENDPOINT
  - This is the URL for the DynamoDB endpoint. If not set, the endpoint is determined by the application from the AWS account and region information.
  - When running locally for development and testing, this would be set to http://localhost:8080/
- XFORM_DYNAMODB_TABLE
  - The name of the table to use. If not set, the default is izgw-hub
  - Please see the _AWS DynamoDB Table Setup_ section in this document for details on this setup

### Migration Configuration

If you have existing configuration in files, you may have the application do a migration to DynamoDB.

You would set XFORM_REPOSITORY_TYPE to ```migration```.

In addition, you will need to configure the options spelled out in the File-based and DynamoDB sections above. This is so that the application will know where to find the files containing data to migrate to DynamoDB.

TODO - spell out steps

1. Ensure the necessary DynamoDB table (See _AWS DynamoDB Table Setup_ section)
2. Configure ECS task definition for migration as detailed above
3. 

## AWS DynamoDB Table Setup

TODO

## Running Locally

TODO

## Package

The [gov.cdc.izgateway.xform.repository](https://github.com/IZGateway/izgw-transform/blob/f97d2918bd031a55de081c907341811ed82b6749/src/main/java/gov/cdc/izgateway/xform/repository) package contains the code for Tranformation Service storage.

