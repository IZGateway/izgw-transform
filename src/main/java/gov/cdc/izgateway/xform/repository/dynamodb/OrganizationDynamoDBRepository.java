package gov.cdc.izgateway.xform.repository.dynamodb;

import gov.cdc.izgateway.xform.model.Organization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;

import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "xform.repository.type", havingValue = "dynamodb")
@Primary
public class OrganizationDynamoDBRepository extends GenericDynamoDBRepository<Organization> {

    public OrganizationDynamoDBRepository(
            DynamoDbEnhancedClient dynamoDbClient,
            @Value("${xform.repository.dynamodb.table}") String tableName) {
        super(dynamoDbClient, tableName, Organization.class, getTableSchema());
    }

    @Override
    protected String getEntityName() {
        return "Organization";
    }

    private static TableSchema<Organization> getTableSchema() {
        return StaticTableSchema.builder(Organization.class)
                .newItemSupplier(Organization::new)
                .addAttribute(String.class, a -> a.name("entityType")
                        .getter(org -> "Organization")
                        .setter((org, val) -> {/* Read-only attribute */})
                        .tags(StaticAttributeTags.primaryPartitionKey()))
                .addAttribute(String.class, a -> a.name("sortKey")
                        .getter(org -> org.getId() != null ? org.getId().toString() : null)
                        .setter((org, val) -> org.setId(val != null ? UUID.fromString(val) : null))
                        .tags(StaticAttributeTags.primarySortKey()))
                .addAttribute(String.class, a -> a.name("organizationName")
                        .getter(Organization::getOrganizationName)
                        .setter(Organization::setOrganizationName))
                .addAttribute(Boolean.class, a -> a.name("active")
                        .getter(Organization::getActive)
                        .setter(Organization::setActive))
                .addAttribute(String.class, a -> a.name("commonName")
                        .getter(Organization::getCommonName)
                        .setter(Organization::setCommonName))
                .build();
    }
}
