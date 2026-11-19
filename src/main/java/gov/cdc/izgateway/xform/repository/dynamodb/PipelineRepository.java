package gov.cdc.izgateway.xform.repository.dynamodb;

import gov.cdc.izgateway.xform.model.Pipeline;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

/**
 * DynamoDB repository for {@link Pipeline} entities.
 */
public class PipelineRepository extends GenericDynamoDBRepository<Pipeline> {

    /**
     * Creates the repository.
     *
     * @param dynamoDbClient the DynamoDB enhanced client
     * @param tableName the name of the DynamoDB table
     */
    public PipelineRepository(
            DynamoDbEnhancedClient dynamoDbClient,
            String tableName) {
        super(dynamoDbClient, tableName, Pipeline.class, TableSchema.fromBean(Pipeline.class));
    }

}
