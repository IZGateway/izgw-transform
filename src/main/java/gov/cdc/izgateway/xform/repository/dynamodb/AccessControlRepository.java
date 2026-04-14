package gov.cdc.izgateway.xform.repository.dynamodb;

import gov.cdc.izgateway.xform.model.AccessControl;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

/**
 * DynamoDB repository for access control entries.
 */
public class AccessControlRepository extends GenericDynamoDBRepository<AccessControl> {
    /**
     * Create the repository for a DynamoDB table.
     *
     * @param dynamoDbClient DynamoDB enhanced client
     * @param tableName DynamoDB table name
     */
    public AccessControlRepository(
            DynamoDbEnhancedClient dynamoDbClient,
            String tableName) {
        super(dynamoDbClient, tableName, AccessControl.class, TableSchema.fromBean(AccessControl.class));
    }

}
