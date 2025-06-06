package gov.cdc.izgateway.xform.repository.dynamodb;

import gov.cdc.izgateway.xform.model.OperationPreconditionField;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

public class OperationPreconditionFieldRepository extends GenericDynamoDBRepository<OperationPreconditionField> {
    public OperationPreconditionFieldRepository(
            DynamoDbEnhancedClient dynamoDbClient,
            String tableName) {
        super(dynamoDbClient, tableName, OperationPreconditionField.class, TableSchema.fromBean(OperationPreconditionField.class));
    }

}
