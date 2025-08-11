package gov.cdc.izgateway.xform.repository.dynamodb.migration;

import gov.cdc.izgateway.xform.model.BaseModel;
import gov.cdc.izgateway.xform.repository.dynamodb.GenericDynamoDBRepository;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Set;

/**
 * Generic migrator for entities that extend from BaseModel
 *
 * @param <T> Entity type to migrate
 */
@Slf4j
public class GenericEntityTruncator<T extends BaseModel> implements EntityTruncator<T> {

    private final GenericDynamoDBRepository<T> dynamoDbRepository;
    private final Class<T> entityClass;

    public GenericEntityTruncator(Class<T> entityClass,
                                  DynamoDbEnhancedClient dynamoDbClient,
                                  String tableName) {
        this.entityClass = entityClass;
        this.dynamoDbRepository = new GenericDynamoDBRepositoryImpl<>(dynamoDbClient, tableName, entityClass);
    }

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

    @Override
    public Class<T> getEntityType() {
        return entityClass;
    }

    @Override
    public String getEntityName() {
        return entityClass.getSimpleName();
    }


    /**
     * Generic DynamoDB repository implementation for migration
     */
    private static class GenericDynamoDBRepositoryImpl<T extends BaseModel> extends GenericDynamoDBRepository<T> {
        private final Class<T> entityClass;

        public GenericDynamoDBRepositoryImpl(DynamoDbEnhancedClient dynamoDbClient,
                                             String tableName,
                                             Class<T> entityClass) {
            super(dynamoDbClient, tableName, entityClass, TableSchema.fromBean(entityClass));
            this.entityClass = entityClass;
        }

        @Override
        protected String getEntityName() {
            return entityClass.getSimpleName();
        }
    }
}
