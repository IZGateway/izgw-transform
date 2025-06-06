package gov.cdc.izgateway.xform.repository.dynamodb;

import gov.cdc.izgateway.xform.model.Pipeline;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

public class PipelineRepository extends GenericDynamoDBRepository<Pipeline> {

    public PipelineRepository(
            DynamoDbEnhancedClient dynamoDbClient,
            String tableName) {
        super(dynamoDbClient, tableName, Pipeline.class, TableSchema.fromBean(Pipeline.class));
    }

}
