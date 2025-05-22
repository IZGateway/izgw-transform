# Connecting to a Local DynamoDB Instance

This document provides instructions on how to configure the application to connect to a locally running DynamoDB instance.

## Prerequisites

- A locally running DynamoDB instance (e.g., using [DynamoDB Local](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.html))
- The application codebase

## Configuration

The application can be configured to connect to a local DynamoDB instance by setting the `xform.repository.dynamodb.endpoint` property. This can be done in several ways:

### Option 1: Environment Variable

Set the `XFORM_DYNAMODB_ENDPOINT` environment variable:

```bash
export XFORM_DYNAMODB_ENDPOINT=http://localhost:8000
```

### Option 2: Application Properties

Update the `application.yml` file directly:

```yaml
xform:
  repository:
    type: dynamodb
    dynamodb:
      region: us-east-1
      endpoint: http://localhost:8000
      organizations-table: izgw-hub
```

### Option 3: Command Line Argument

Pass the property as a command line argument when starting the application:

```bash
java -jar xform.jar --xform.repository.dynamodb.endpoint=http://localhost:8000
```

## Running DynamoDB Local

If you don't have DynamoDB Local set up yet, you can run it using Docker:

```bash
docker run -p 8000:8000 amazon/dynamodb-local
```

This will start a DynamoDB instance accessible at `http://localhost:8000`.

## Creating Tables

When using DynamoDB Local, you'll need to create the required tables before starting the application. You can use the AWS CLI to create the tables:

```bash
aws dynamodb create-table \
    --table-name izgw-hub \
    --attribute-definitions AttributeName=id,AttributeType=S \
    --key-schema AttributeName=id,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --endpoint-url http://localhost:8000
```

## AWS Credentials

When connecting to DynamoDB Local, you still need to provide AWS credentials, but they can be dummy values. You can set them using environment variables:

```bash
export AWS_ACCESS_KEY_ID=dummy
export AWS_SECRET_ACCESS_KEY=dummy
export AWS_REGION=us-east-1
```

## Troubleshooting

If you encounter issues connecting to your local DynamoDB instance:

1. Ensure the DynamoDB Local instance is running and accessible at the configured endpoint
2. Check that the required tables have been created
3. Verify that AWS credentials are properly configured
4. Look for connection errors in the application logs

For more information on DynamoDB Local, refer to the [AWS documentation](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.html).
