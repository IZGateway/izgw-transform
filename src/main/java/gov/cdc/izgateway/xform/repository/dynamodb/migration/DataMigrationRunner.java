package gov.cdc.izgateway.xform.repository.dynamodb.migration;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Executes migration from file storage to DynamoDB on application startup
 * if the repository type is set to 'migration'
 */
@Slf4j
@Component
@ConditionalOnExpression("'${spring.database:}'.equalsIgnoreCase('migrate')")
public class DataMigrationRunner implements ApplicationRunner {
    @Value("${spring.database:}")
    private String springDatabase;

    private final DataMigrationService migrationService;
    private final MigrationLockService lockService;

    @Autowired
    public DataMigrationRunner(DataMigrationService migrationService, MigrationLockService lockService) {
        this.migrationService = migrationService;
        this.lockService = lockService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        log.info("Starting Data Migration from File Storage to DynamoDB");

        if ( !lockService.acquireLock() ) {
            // Wait until the migration is complete
            log.info("Waiting for migration lock to be released by another node...");
            while (lockService.isMigrationInProgress()) {
                try {
                    Thread.sleep(5000); // Check every 5 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Migration wait interrupted", e);
                }
            }
            log.warn("Migration has been completed by another node. Exiting.");
            return;
        }

        try {
            boolean success = migrationService.migrateAll();

            if (success) {
                log.info("Data Migration Completed Successfully");
                log.info("IMPORTANT: Set 'xform.repository.type=dynamodb' for future startups to use DynamoDB only");
            } else {
                log.error("Data Migration Completed with Errors");
                log.error("Please review the errors above and fix any issues before retrying");
            }

        } catch (Exception e) {
            log.error("Data Migration Failed", e);
            throw new RuntimeException("Migration failed: " + e.getMessage(), e);
        } finally {
            lockService.releaseLock();
        }
    }
}
