package gov.cdc.izgateway.xform.repository.dynamodb;

import gov.cdc.izgateway.xform.model.Event;
import gov.cdc.izgateway.xform.model.User;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

public class EventRepository extends GenericDynamoDBRepository<Event> {
    public EventRepository(
            DynamoDbEnhancedClient dynamoDbClient,
            String tableName) {
        super(dynamoDbClient, tableName, Event.class, TableSchema.fromBean(Event.class));
    }
}
