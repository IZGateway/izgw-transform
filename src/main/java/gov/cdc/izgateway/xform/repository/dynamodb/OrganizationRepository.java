package gov.cdc.izgateway.xform.repository.dynamodb;

import gov.cdc.izgateway.xform.model.Organization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
@ConditionalOnProperty(name = "xform.repository.type", havingValue = "dynamodb")
@Primary
public class OrganizationRepository extends GenericDynamoDBRepository<Organization> {

    public OrganizationRepository(
            DynamoDbEnhancedClient dynamoDbClient,
            @Value("${xform.repository.dynamodb.table}") String tableName) {
        super(dynamoDbClient, tableName, Organization.class, TableSchema.fromBean(Organization.class));
    }

    @Override
    protected String getEntityName() {
        return "Organization";
    }
}
