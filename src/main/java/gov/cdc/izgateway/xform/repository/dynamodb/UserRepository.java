package gov.cdc.izgateway.xform.repository.dynamodb;

import gov.cdc.izgateway.xform.model.User;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

/**
 * DynamoDB repository for {@link User} entities.
 */
public class UserRepository extends GenericDynamoDBRepository<User> {
    /**
     * Creates the repository.
     *
     * @param dynamoDbClient the DynamoDB enhanced client
     * @param tableName the name of the DynamoDB table
     */
    public UserRepository(
            DynamoDbEnhancedClient dynamoDbClient,
            String tableName) {
        super(dynamoDbClient, tableName, User.class, TableSchema.fromBean(User.class));
    }
}
