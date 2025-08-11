package gov.cdc.izgateway.xform.repository.dynamodb.migration;

import gov.cdc.izgateway.xform.model.BaseModel;
import gov.cdc.izgateway.xform.repository.dynamodb.GenericDynamoDBRepository;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Set;

/**
 * Truncates (deletes) all entities of a given type from DynamoDB storage.
 * Uses a generic repository to perform deletion for any entity extending BaseModel.
 */
@Slf4j
public class GenericEntityTruncator<T extends BaseModel> implements EntityTruncator<T> {

    private final GenericDynamoDBRepository<T> dynamoDbRepository;
    private final Class<T> entityClass;

    /**
     * Constructs a GenericEntityTruncator for the specified entity type and DynamoDB table.
     * @param entityClass the class of the entity
     * @param dynamoDbClient the DynamoDB enhanced client
     * @param tableName the name of the DynamoDB table
     */
    public GenericEntityTruncator(Class<T> entityClass,
                                  DynamoDbEnhancedClient dynamoDbClient,
                                  String tableName) {
        this.entityClass = entityClass;
        this.dynamoDbRepository = new GenericDynamoDBRepositoryImpl<>(dynamoDbClient, tableName, entityClass);
    }

    /**
     * Deletes all entities of the specified type from the DynamoDB table.
     * Logs progress and continues on errors.
     */
    @Override
    public void truncate() {
        Set<T> entities = dynamoDbRepository.getEntitySet();
        int totalCount = entities.size();
        int truncatedCount = 0;

        log.info("Found {} {} entities in file storage", totalCount, getEntityName());

        for (T entity : entities) {
            try {
                dynamoDbRepository.deleteEntity(entity.getId());
                truncatedCount++;
                log.info("Truncated {}: {}", getEntityName(), entity.getId());

            } catch (Exception e) {
                log.warn("Failed to truncate {} {}: {}",
                        getEntityName(), entity.getId(), e.getMessage());
                // Keep going with other entities
            }
        }

        log.info("Successfully truncated {}/{} {}",
                truncatedCount, totalCount, getEntityName());

    }

    /**
     * Returns the class type of the entity.
     * @return the entity class
     */
    @Override
    public Class<T> getEntityType() {
        return entityClass;
    }

    /**
     * Returns the simple name of the entity type.
     * @return the entity name
     */
    @Override
    public String getEntityName() {
        return entityClass.getSimpleName();
    }

    /**
     * Internal implementation of the generic DynamoDB repository for a specific entity type.
     */
    private static class GenericDynamoDBRepositoryImpl<T extends BaseModel> extends GenericDynamoDBRepository<T> {
        private final Class<T> entityClass;

        /**
         * Constructs the repository implementation for the given entity type and table.
         * @param dynamoDbClient the DynamoDB enhanced client
         * @param tableName the name of the DynamoDB table
         * @param entityClass the class of the entity
         */
        public GenericDynamoDBRepositoryImpl(DynamoDbEnhancedClient dynamoDbClient,
                                             String tableName,
                                             Class<T> entityClass) {
            super(dynamoDbClient, tableName, entityClass, TableSchema.fromBean(entityClass));
            this.entityClass = entityClass;
        }

        /**
         * Returns the simple name of the entity type.
         * @return the entity name
         */
        @Override
        protected String getEntityName() {
            return entityClass.getSimpleName();
        }
    }
}