package gov.cdc.izgateway.xform.repository.dynamodb;

import gov.cdc.izgateway.xform.model.BaseModel;
import gov.cdc.izgateway.xform.model.Solution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;

import java.util.UUID;

@Repository
@ConditionalOnExpression("'${xform.repository.type}'.equals('dynamodb') || '${xform.repository.type}'.equals('migration')")
public class SolutionRepository extends GenericDynamoDBRepository<Solution> {

    public SolutionRepository(
            DynamoDbEnhancedClient dynamoDbClient,
            @Value("${xform.repository.dynamodb.table}") String tableName) {
        super(dynamoDbClient, tableName, Solution.class, TableSchema.fromClass(Solution.class));
    }
}
