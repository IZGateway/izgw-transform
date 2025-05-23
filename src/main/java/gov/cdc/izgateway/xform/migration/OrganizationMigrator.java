package gov.cdc.izgateway.xform.migration;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.Organization;
import gov.cdc.izgateway.xform.repository.file.GenericFileRepository;
import gov.cdc.izgateway.xform.repository.dynamodb.GenericDynamoDBRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Migrates Organizations from file storage to DynamoDB.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "xform.repository.type", havingValue = "migration")
public class OrganizationMigrator implements EntityMigrator<Organization> {
    
    private final OrganizationDynamoDBRepository dynamoDbRepository;
    private final OrganizationFileRepository fileRepository;
    
    public OrganizationMigrator(DynamoDbEnhancedClient dynamoDbClient,
                               @Value("${xform.repository.dynamodb.table}") String tableName,
                               @Value("${xform.configurations.organizations}") String organizationsFilePath) {
        this.dynamoDbRepository = new OrganizationDynamoDBRepository(dynamoDbClient, tableName);
        this.fileRepository = new OrganizationFileRepository(organizationsFilePath);
    }
    
    @Override
    public MigrationCounts migrate() {
        Set<Organization> organizations = fileRepository.getEntitySet();
        int totalCount = organizations.size();
        int migratedCount = 0;
        int skippedCount = 0;
        
        log.info("Found {} organizations in file storage", totalCount);
        
        for (Organization org : organizations) {
            try {
                Organization existing = dynamoDbRepository.getEntity(org.getId());
                if (existing != null) {
                    log.info("Organization {} already exists in DynamoDB, skipping", org.getId());
                    skippedCount++;
                    continue;
                }
                
                dynamoDbRepository.createEntity(org);
                migratedCount++;
                log.info("Migrated organization: {} ({})", org.getOrganizationName(), org.getId());
                
            } catch (Exception e) {
                log.warn("Failed to migrate organization {} ({}): {}", 
                        org.getOrganizationName(), org.getId(), e.getMessage());
                // Keep going with other organizations
            }
        }
        
        log.info("Successfully migrated {}/{} organizations ({} skipped)",
                migratedCount, totalCount, skippedCount);
        
        return new MigrationCounts(totalCount, migratedCount, skippedCount);
    }
    
    @Override
    public Class<Organization> getEntityType() {
        return Organization.class;
    }
    
    @Override
    public String getEntityName() {
        return Organization.class.getSimpleName();
    }
    
    /**
     * Temp file repository for reading organizations during migration
     * Necessary because of dependency injection/configuration Spring constraints
     */
    private static class OrganizationFileRepository extends GenericFileRepository<Organization> {
        
        public OrganizationFileRepository(String filePath) {
            this.filePath = filePath;
        }
        
        @Override
        protected TypeReference<LinkedHashSet<Organization>> getTypeReference() {
            return new TypeReference<>() {};
        }
    }
    
    /**
     * Temp DynamoDB repository for writing organizations during migration
     * Necessary because of dependency injection/configuration Spring constraints
     */
    private static class OrganizationDynamoDBRepository extends GenericDynamoDBRepository<Organization> {
        
        public OrganizationDynamoDBRepository(DynamoDbEnhancedClient dynamoDbClient, String tableName) {
            super(dynamoDbClient, tableName, Organization.class, TableSchema.fromBean(Organization.class));
        }
        
        @Override
        protected String getEntityName() {
            return Organization.class.getSimpleName();
        }
    }
}
