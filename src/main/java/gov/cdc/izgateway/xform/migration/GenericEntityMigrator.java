package gov.cdc.izgateway.xform.migration;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.BaseModel;
import gov.cdc.izgateway.xform.repository.file.GenericFileRepository;
import gov.cdc.izgateway.xform.repository.dynamodb.GenericDynamoDBRepository;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Generic migrator for entities that extend from BaseModel
 *
 * @param <T> Entity type to migrate
 */
@Slf4j
public class GenericEntityMigrator<T extends BaseModel> implements EntityMigrator<T> {
    
    private final GenericFileRepository<T> fileRepository;
    private final GenericDynamoDBRepository<T> dynamoDbRepository;
    private final Class<T> entityClass;
    
    public GenericEntityMigrator(Class<T> entityClass,
                                String filePath,
                                DynamoDbEnhancedClient dynamoDbClient,
                                String tableName) {
        this.entityClass = entityClass;
        this.fileRepository = new GenericFileRepositoryImpl<>(filePath, entityClass);
        this.dynamoDbRepository = new GenericDynamoDBRepositoryImpl<>(dynamoDbClient, tableName, entityClass);
    }
    
    @Override
    public MigrationCounts migrate() {
        Set<T> entities = fileRepository.getEntitySet();
        int totalCount = entities.size();
        int migratedCount = 0;
        int skippedCount = 0;
        
        log.info("Found {} {} entities in file storage", totalCount, getEntityName());
        
        for (T entity : entities) {
            try {
                T existing = dynamoDbRepository.getEntity(entity.getId());
                if (existing != null) {
                    log.info("{} {} already exists in DynamoDB, skipping", getEntityName(), entity.getId());
                    skippedCount++;
                    continue;
                }
                
                dynamoDbRepository.createEntity(entity);
                migratedCount++;
                log.info("Migrated {}: {}", getEntityName(), entity.getId());
                
            } catch (Exception e) {
                log.warn("Failed to migrate {} {}: {}", 
                        getEntityName(), entity.getId(), e.getMessage());
                // Keep going with other entities
            }
        }
        
        log.info("Successfully migrated {}/{} {} ({} skipped)",
                migratedCount, totalCount, getEntityName(), skippedCount);
        
        return new MigrationCounts(totalCount, migratedCount, skippedCount);
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
     * Generic file repository implementation for migration
     */
    private static class GenericFileRepositoryImpl<T extends BaseModel> extends GenericFileRepository<T> {
        private final Class<T> entityClass;
        
        public GenericFileRepositoryImpl(String filePath, Class<T> entityClass) {
            this.filePath = filePath;
            this.entityClass = entityClass;
        }
        
        @Override
        protected TypeReference<LinkedHashSet<T>> getTypeReference() {
            // Create a TypeReference that uses the concrete entity class
            return new TypeReference<LinkedHashSet<T>>() {
                @Override
                public java.lang.reflect.Type getType() {
                    return com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance()
                            .constructCollectionType(LinkedHashSet.class, entityClass);
                }
            };
        }
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
