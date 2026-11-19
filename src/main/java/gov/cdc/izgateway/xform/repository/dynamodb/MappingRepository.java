package gov.cdc.izgateway.xform.repository.dynamodb;

import gov.cdc.izgateway.xform.model.Mapping;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

/**
 * DynamoDB repository for {@link Mapping} entities.
 */
public class MappingRepository extends GenericDynamoDBRepository<Mapping> {
    /**
     * Creates the repository.
     *
     * @param dynamoDbClient the DynamoDB enhanced client
     * @param tableName the name of the DynamoDB table
     */
    public MappingRepository(
            DynamoDbEnhancedClient dynamoDbClient,
            String tableName) {
        super(dynamoDbClient, tableName, Mapping.class, TableSchema.fromBean(Mapping.class));
    }
}
