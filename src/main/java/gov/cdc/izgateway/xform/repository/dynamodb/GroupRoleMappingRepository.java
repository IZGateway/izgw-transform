package gov.cdc.izgateway.xform.repository.dynamodb;

import gov.cdc.izgateway.xform.model.GroupRoleMapping;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

/**
 * DynamoDB repository for {@link GroupRoleMapping} entities.
 */
public class GroupRoleMappingRepository extends GenericDynamoDBRepository<GroupRoleMapping> {

    /**
     * Creates the repository.
     *
     * @param dynamoDbClient the DynamoDB enhanced client
     * @param tableName the name of the DynamoDB table
     */
    public GroupRoleMappingRepository(
            DynamoDbEnhancedClient dynamoDbClient,
            String tableName
    ) {
        super(dynamoDbClient, tableName, GroupRoleMapping.class, TableSchema.fromBean(GroupRoleMapping.class));
    }

}
