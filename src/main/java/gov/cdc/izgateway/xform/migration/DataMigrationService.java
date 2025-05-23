package gov.cdc.izgateway.xform.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service that orchestrates migration of all entity types from file storage to DynamoDB.
 */
@Slf4j
@Service
public class DataMigrationService {
    
    private final List<EntityMigrator<?>> migrators;
    
    @Autowired
    public DataMigrationService(List<EntityMigrator<?>> migrators) {
        this.migrators = migrators;
    }
    
    /**
     * Executes migration for all registered entity migrators.
     *
     * @return Overall migration success status
     */
    public boolean migrateAll() {
        log.info("Starting data migration for {} entity types", migrators.size());
        
        boolean overallSuccess = true;
        int totalEntities = 0;
        int totalMigrated = 0;
        int totalSkipped = 0;
        
        for (EntityMigrator<?> migrator : migrators) {
            log.info("Migrating {} entities...", migrator.getEntityName());
            
            try {
                MigrationResult result = migrator.migrate();
                
                if (result.isSuccess()) {
                    log.info("✓ {}", result.getMessage());
                    totalEntities += result.getTotalCount();
                    totalMigrated += result.getMigratedCount();
                    totalSkipped += result.getSkippedCount();
                } else {
                    log.error("✗ {}", result.getMessage());
                    if (result.getError() != null) {
                        log.error("Migration error details:", result.getError());
                    }
                    overallSuccess = false;
                }
            } catch (Exception e) {
                log.error("Unexpected error during {} migration: {}", migrator.getEntityName(), e.getMessage(), e);
                overallSuccess = false;
            }
        }
        
        if (overallSuccess) {
            log.info("✓ Migration completed successfully: {}/{} entities migrated ({} skipped)", 
                    totalMigrated, totalEntities, totalSkipped);
        } else {
            log.error("✗ Migration completed with errors");
        }
        
        return overallSuccess;
    }
}