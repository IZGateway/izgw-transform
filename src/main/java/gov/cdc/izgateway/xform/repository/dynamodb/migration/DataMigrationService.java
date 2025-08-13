package gov.cdc.izgateway.xform.repository.dynamodb.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service to migrate entity types from file storage to DynamoDB.
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
     * Run migration for all existing entity migrators.
     *
     * @return Migration success status
     */
    public boolean migrateAll() {

        log.info("Starting data migration for {} entity types", migrators.size());

        boolean overallSuccess = true;
        int totalEntities = 0;
        int totalMigrated = 0;
        int totalSkipped = 0;

        for (EntityMigrator<?> migrator : migrators) {
            log.info("Migrating {}...", migrator.getEntityName());

            try {
                MigrationCounts counts = migrator.migrate();
                totalEntities += counts.total();
                totalMigrated += counts.migrated();
                totalSkipped += counts.skipped();
            } catch (Exception e) {
                log.error("Failed to migrate {}: {}", migrator.getEntityName(), e.getMessage(), e);
                overallSuccess = false;
            }
        }

        if (overallSuccess) {
            log.info("Migration completed successfully: {}/{} entities migrated ({} skipped)",
                    totalMigrated, totalEntities, totalSkipped);
        } else {
            log.error("Migration completed with errors");
        }

        return overallSuccess;
    }
}
