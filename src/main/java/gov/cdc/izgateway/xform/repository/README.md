# Repository Layer

TODO - Austin to update this doc

- Check for existance of DynamoDB table, create if it doesn't exist
- Make partition and sort key names configurable

This package contains the repository layer for the application, which provides data access to the underlying storage.

## Storage Options

The application supports two storage options:

1. **File Storage**: Stores data in JSON files on the filesystem.
2. **DynamoDB Storage**: Stores data in Amazon DynamoDB tables.

## Configuration

The storage option is configured using the `xform.repository.type` property:

- `file`: Use file storage (default)
- `dynamodb`: Use DynamoDB storage

### File Storage Configuration

When using file storage, the following properties need to be configured:

```properties
xform.repository.type=file
xform.configurations.organizations=data/organizations.json
# Add other entity file paths as needed
```

### DynamoDB Storage Configuration

When using DynamoDB storage, the following properties need to be configured:

```properties
xform.repository.type=dynamodb
xform.dynamodb.region=us-east-1
xform.dynamodb.organizations-table=xform-organizations
# Add other entity table names as needed
```

## DynamoDB Table Setup

Before using DynamoDB storage, you need to create the necessary tables in your AWS account. Each table should have the following schema:

### Organizations Table

- Partition Key: `id` (String)
- No Sort Key

You can create the table using the AWS Management Console, AWS CLI, or AWS CloudFormation.

Example AWS CLI command:

```bash
aws dynamodb create-table \
    --table-name xform-organizations \
    --attribute-definitions AttributeName=id,AttributeType=S \
    --key-schema AttributeName=id,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    --region us-east-1
```

## AWS Credentials

The application uses the default AWS credential provider chain to authenticate with AWS. This means it will look for credentials in the following order:

1. Environment variables: `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`
2. Java system properties: `aws.accessKeyId` and `aws.secretKey`
3. The default credential profiles file: `~/.aws/credentials`
4. Amazon ECS container credentials
5. Instance profile credentials

Make sure to configure your AWS credentials appropriately for your environment.

## Adding Support for Other Entities

To add DynamoDB support for other entities:

1. Create a new repository class that extends `GenericDynamoDBRepository<T>` for your entity
2. Implement the necessary table schema
3. Add the appropriate configuration properties
4. Update the README with the new entity's table schema
