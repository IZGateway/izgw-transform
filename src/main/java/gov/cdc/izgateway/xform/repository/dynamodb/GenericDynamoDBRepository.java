package gov.cdc.izgateway.xform.repository.dynamodb;

import gov.cdc.izgateway.xform.model.BaseModel;
import gov.cdc.izgateway.xform.repository.RepositoryRuntimeException;
import gov.cdc.izgateway.xform.repository.XformRepository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class GenericDynamoDBRepository<T extends BaseModel> implements XformRepository<T> {

    protected final DynamoDbEnhancedClient dynamoDbClient;
    protected final String tableName;
    protected final Class<T> entityClass;
    protected final TableSchema<T> tableSchema;
    protected DynamoDbTable<T> table;

    /**
     * Returns the entity name to be used as the partition key.
     * Derived from the entity class simple name.
     * @return the entity name
     */
    protected String getEntityName() {
        return entityClass.getSimpleName();
    }

    protected GenericDynamoDBRepository(DynamoDbEnhancedClient dynamoDbClient, String tableName, Class<T> entityClass, TableSchema<T> tableSchema) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
        this.entityClass = entityClass;
        this.tableSchema = tableSchema;
        this.table = dynamoDbClient.table(tableName, tableSchema);
    }

    @Override
    public T getEntity(UUID id) {
        try {
            Key key = Key.builder()
                    .partitionValue(getEntityName())
                    .sortValue(id.toString())
                    .build();
            return table.getItem(key);
        } catch (DynamoDbException e) {
            throw new RepositoryRuntimeException(String.format("Error retrieving entity with ID %s: %s", id, e.getMessage()), e);
        }
    }

    @Override
    public Set<T> getEntitySet() {
        try {
            // Create a filter expression to only return items with the matching entity type
            // The entity type is stored as the partition key
            ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
                    .filterExpression(
                            software.amazon.awssdk.enhanced.dynamodb.Expression.builder()
                                    .expression(String.format("%s = :entityType", BaseModel.ENTITY_TYPE))
                                    .putExpressionValue(":entityType",
                                            software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder()
                                                    .s(getEntityName())
                                                    .build())
                                    .build())
                    .build();

            PageIterable<T> results = table.scan(scanRequest);

            // Convert the results to a LinkedHashSet
            return results.items().stream()
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } catch (DynamoDbException e) {
            throw new RepositoryRuntimeException(String.format("Error retrieving entities: %s", e.getMessage()), e);
        }
    }

    @Override
    public void createEntity(T obj) {
        try {
            table.putItem(obj);
        } catch (DynamoDbException e) {
            throw new RepositoryRuntimeException(String.format("Error creating entity: %s", e.getMessage()), e);
        }
    }

    @Override
    public void updateEntity(T obj) {
        try {
            table.updateItem(obj);
        } catch (DynamoDbException e) {
            throw new RepositoryRuntimeException(String.format("Error updating entity with ID %s: %s", obj.getId(), e.getMessage()), e);
        }
    }

    @Override
    public void deleteEntity(UUID id) {
        try {
            Key key = Key.builder()
                    .partitionValue(getEntityName())
                    .sortValue(id.toString())
                    .build();
            table.deleteItem(key);
        } catch (DynamoDbException e) {
            throw new RepositoryRuntimeException(String.format("Error deleting entity with ID %s: %s", id, e.getMessage()), e);
        }
    }
}
