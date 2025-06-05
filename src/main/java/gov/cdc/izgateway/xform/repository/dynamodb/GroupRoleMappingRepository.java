package gov.cdc.izgateway.xform.repository.dynamodb;

import gov.cdc.izgateway.xform.model.GroupRoleMapping;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

public class GroupRoleMappingRepository extends GenericDynamoDBRepository<GroupRoleMapping> {

    public GroupRoleMappingRepository(
            DynamoDbEnhancedClient dynamoDbClient,
            String tableName
    ) {
        super(dynamoDbClient, tableName, GroupRoleMapping.class, TableSchema.fromBean(GroupRoleMapping.class));
    }

}
