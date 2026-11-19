package gov.cdc.izgateway.xform.repository.dynamodb;

import gov.cdc.izgateway.xform.model.Event;
import gov.cdc.izgateway.xform.model.User;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

/**
 * The DynamoDB repository for {@link Event} entities.
 */
public class EventRepository extends GenericDynamoDBRepository<Event> {
    /**
     * Creates the repository.
     *
     * @param dynamoDbClient the DynamoDB enhanced client
     * @param tableName the name of the DynamoDB table
     */
    public EventRepository(
            DynamoDbEnhancedClient dynamoDbClient,
            String tableName) {
        super(dynamoDbClient, tableName, Event.class, TableSchema.fromBean(Event.class));
    }
}
