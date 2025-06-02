package gov.cdc.izgateway.xform.repository.dynamodb;

import gov.cdc.izgateway.xform.model.AccessControl;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

public class AccessControlRepository extends GenericDynamoDBRepository<AccessControl> {
    public AccessControlRepository(
            DynamoDbEnhancedClient dynamoDbClient,
            String tableName) {
        super(dynamoDbClient, tableName, AccessControl.class, TableSchema.fromBean(AccessControl.class));
    }

}
