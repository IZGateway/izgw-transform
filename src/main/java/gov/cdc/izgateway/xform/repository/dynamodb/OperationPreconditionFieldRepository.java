package gov.cdc.izgateway.xform.repository.dynamodb;

import gov.cdc.izgateway.xform.model.OperationPreconditionField;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

/**
 * DynamoDB repository for {@link OperationPreconditionField} entities.
 */
public class OperationPreconditionFieldRepository extends GenericDynamoDBRepository<OperationPreconditionField> {
    /**
     * Creates the repository.
     *
     * @param dynamoDbClient the DynamoDB enhanced client
     * @param tableName the name of the DynamoDB table
     */
    public OperationPreconditionFieldRepository(
            DynamoDbEnhancedClient dynamoDbClient,
            String tableName) {
        super(dynamoDbClient, tableName, OperationPreconditionField.class, TableSchema.fromBean(OperationPreconditionField.class));
    }

}
