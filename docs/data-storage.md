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

#### Migration Steps

This assumes running multiple ECS containers

1. Ensure the necessary DynamoDB table (See _AWS DynamoDB Table Setup_ section)
2. Leave an existing, file-based task running so that the system is processing requests during migration
3. Configure ECS task definition for migration as detailed above
4. Start a new ECS task in the service using the migration definition
5. Verify the updated ECS task started propery and that the migration completed successfully
6. Configure the ECS task definition for dynamodb as detailed above
7. Have _all_ instances start with the new dynamodb ECS task definition
8. Verify the system is operational via smoketest

The above assumes you are running multiple ECS containers. If you are running a single node you would simply update the task definition for migration and restart the node. At some point coming back to change the task definition to just dynamodb. 

## AWS DynamoDB Table Setup

The DynamoDB table required for Transformation Service requires these settings for the Partition Key and Sort Key:

- Partition Key - Needs to be a String and named entityType
- Sort Key - Needs to be a String and named sortKey

Following are the current recommended settings:

```json
{
  "AttributeDefinitions": [
    {
      "AttributeName": "entityType",
      "AttributeType": "S"
    },
    {
      "AttributeName": "sortKey",
      "AttributeType": "S"
    }
  ],
  "TableName": "izgw-hub",
  "KeySchema": [
    {
      "AttributeName": "entityType",
      "KeyType": "HASH"
    },
    {
      "AttributeName": "sortKey",
      "KeyType": "RANGE"
    }
  ],
  "TableStatus": "ACTIVE",
  "ProvisionedThroughput": {
    "ReadCapacityUnits": 10,
    "WriteCapacityUnits": 10
  },
  "TableClassSummary": {
    "TableClass": "STANDARD"
  },
  "DeletionProtectionEnabled": true
}
```

Command to create a table named ```izgw-hub``` using the recommended settings:

```shell
aws dynamodb create-table \
  --table-name izgw-hub \
  --attribute-definitions \
    AttributeName=entityType,AttributeType=S \
    AttributeName=sortKey,AttributeType=S \
  --key-schema \
    AttributeName=entityType,KeyType=HASH \
    AttributeName=sortKey,KeyType=RANGE \
  --provisioned-throughput \
    ReadCapacityUnits=10,WriteCapacityUnits=10 \
  --table-class STANDARD \
  --deletion-protection-enabled
```

## Running Locally

To develop and test locally, you can run DynamoDB either via the NoSQL Workbench or through Docker.

### Run DynamoDB

NoSQL Workbench can be found here: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/workbench.settingup.html

When running NoSQL Workbench, you should see an option on the main screen on the bottom left that will allow you to toggle running locally:

![nosql_workbench_ddb_local_toggle.png](images/nosql_workbench_ddb_local_toggle.png)

### Configuration

There are 4 properies you'll need to set via your environment in order to connect to DynamoDB locally:

- XFORM_DYNAMODB_ENDPOINT 
  - Tells the application where to connect to the local DynamoDB. By default, the local DynamoDB will run on port 8000 so you would set this variable to ```http://localhost:8000```
- AWS_ACCESS_KEY_ID
  - Required to exist even if we are connecting locally. This doesn't need to be a valid access key, you can set this to literally a string like ```dummy```
- AWS_SECRET_ACCESS_KEY
  - Required to exist even if we are connecting locally. This doesn't need to be a valid access key, you can set this to literally a string like ```dummy```
- AWS_REGION
  - Required even running locally and must be a valid region. You can use ```us-east-1```

### Creating Table Locally

A table can be created locally using a modification on the command specified earlier:

```shell
aws dynamodb create-table \
  --table-name izgw-hub \
  --attribute-definitions \
    AttributeName=entityType,AttributeType=S \
    AttributeName=sortKey,AttributeType=S \
  --key-schema \
    AttributeName=entityType,KeyType=HASH \
    AttributeName=sortKey,KeyType=RANGE \
  --provisioned-throughput \
    ReadCapacityUnits=10,WriteCapacityUnits=10 \
  --table-class STANDARD \
  --deletion-protection-enabled \
  --endpoint-url http://localhost:8000
```

Note that you _will_ need to have the AWS Region and Credentials set (even if we are connecting locally).

You can specify them by setting the following environment variables:

- AWS_ACCESS_KEY_ID
  - Required to exist even if we are connecting locally. This doesn't need to be a valid access key, you can set this to literally a string like ```dummy```
- AWS_SECRET_ACCESS_KEY
  - Required to exist even if we are connecting locally. This doesn't need to be a valid access key, you can set this to literally a string like ```dummy```
- AWS_REGION
  - Required even running locally and must be a valid region. You can use ```us-east-1```

### Example Docker Compose

If you want to run DynamoDB inside Docker, this is an example docker compose file:

```yaml
services:
 dynamodb-local:
   command: "-jar DynamoDBLocal.jar -sharedDb -dbPath ./data"
   image: "amazon/dynamodb-local:latest"
   container_name: dynamodb-local
   ports:
     - "8000:8000"
   volumes:
     - "./docker/dynamodb:/home/dynamodblocal/data"
   working_dir: /home/dynamodblocal
```

## Package

The [gov.cdc.izgateway.xform.repository](https://github.com/IZGateway/izgw-transform/blob/f97d2918bd031a55de081c907341811ed82b6749/src/main/java/gov/cdc/izgateway/xform/repository) package contains the code for Tranformation Service storage.

