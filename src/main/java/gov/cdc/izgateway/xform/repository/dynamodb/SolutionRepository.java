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
        super(dynamoDbClient, tableName, Solution.class, getTableSchema());
    }

    @Override
    protected String getEntityName() {
        return Solution.class.getSimpleName();
    }

    // TODO - look at getting rid of this somehow.
    // Can't use TableSchema.fromBean(Solution.class), similar to Pipeline
    // because Solutions are made up of SolutionOperations which are then made up of
    // complex nested objects
    private static TableSchema<Solution> getTableSchema() {
        return StaticTableSchema.builder(Solution.class)
                .newItemSupplier(Solution::new)
                .addAttribute(String.class, a -> a.name(BaseModel.ENTITY_TYPE)
                        .getter(solution -> "Solution")
                        .setter((solution, val) -> {/* Read-only attribute */})
                        .tags(StaticAttributeTags.primaryPartitionKey()))
                .addAttribute(String.class, a -> a.name(BaseModel.SORT_KEY)
                        .getter(solution -> solution.getId() != null ? solution.getId().toString() : null)
                        .setter((solution, val) -> solution.setId(val != null ? UUID.fromString(val) : null))
                        .tags(StaticAttributeTags.primarySortKey()))
                .addAttribute(String.class, a -> a.name("solutionName")
                        .getter(Solution::getSolutionName)
                        .setter(Solution::setSolutionName))
                .addAttribute(String.class, a -> a.name("description")
                        .getter(Solution::getDescription)
                        .setter(Solution::setDescription))
                .addAttribute(String.class, a -> a.name("version")
                        .getter(Solution::getVersion)
                        .setter(Solution::setVersion))
                .addAttribute(Boolean.class, a -> a.name("active")
                        .getter(Solution::getActive)
                        .setter(Solution::setActive))
                .addAttribute(String.class, a -> a.name("requestOperations")
                        .getter(Solution::getRequestOperationsJson)
                        .setter(Solution::setRequestOperationsJson))
                .addAttribute(String.class, a -> a.name("responseOperations")
                        .getter(Solution::getResponseOperationsJson)
                        .setter(Solution::setResponseOperationsJson))
                .build();
    }
}
