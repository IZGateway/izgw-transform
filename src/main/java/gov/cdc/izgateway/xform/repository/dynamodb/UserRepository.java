package gov.cdc.izgateway.xform.repository.dynamodb;

import gov.cdc.izgateway.xform.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
@ConditionalOnExpression("'${xform.repository.type}'.equals('dynamodb') || '${xform.repository.type}'.equals('migration')")
public class UserRepository extends GenericDynamoDBRepository<User> {
    public UserRepository(
            DynamoDbEnhancedClient dynamoDbClient,
            @Value("${xform.repository.dynamodb.table}") String tableName) {
        super(dynamoDbClient, tableName, User.class, TableSchema.fromBean(User.class));
    }

    @Override
    protected String getEntityName() {
        return User.class.getSimpleName();
    }

}
