# Transformation Service - Storage/Repository

This section/package contains the repository layer for the application, which provides data access to the underlying storage.

TODO - Austin to update this doc

- Pickup w/ Postman to make sure JWT section works
- IAM requirements for this to run in AWS (maybe this is already documented for Hub)
- Look at Hub documentation re: running locally + configuring in AWS re: setting region & credentials
  - Remember for documetation that if running in dynamodb mode that you'll have to set AWS env
- Look at adding logging for Operations/Preconditions (it's missed because they override getEntitySet)
- Sorting of objects returned via getEntitySet? currently file-based is the order its in the file.  DynamoDB is the sortKey which is the UUID of the object.
- Document creation of table
- Can you run this in dynamodb mode and NOT specify the files

Tests

- local ddb with no tables
- local ddb with tables, but not configured table
- remote ddb with tables, but not configured
- Run Get's for all API endpoints on files
  - Migrate (local) - run Get's for all API endpoints on ddb and diff (will require jq sort by uuid for both)

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
