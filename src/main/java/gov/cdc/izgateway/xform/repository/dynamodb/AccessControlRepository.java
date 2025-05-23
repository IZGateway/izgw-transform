package gov.cdc.izgateway.xform.repository.dynamodb;

import gov.cdc.izgateway.xform.model.AccessControl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
@ConditionalOnProperty(name = "xform.repository.type", havingValue = "dynamodb")
public class AccessControlRepository extends GenericDynamoDBRepository<AccessControl> {
    public AccessControlRepository(
            DynamoDbEnhancedClient dynamoDbClient,
            @Value("${xform.repository.dynamodb.table}") String tableName) {
        super(dynamoDbClient, tableName, AccessControl.class, TableSchema.fromBean(AccessControl.class));
    }

    @Override
    protected String getEntityName() {
        return getClass().getSimpleName();
    }

}
