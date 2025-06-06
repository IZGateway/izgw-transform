package gov.cdc.izgateway.xform.repository.dynamodb.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Executes migration from file storage to DynamoDB on application startup
 * if the repository type is set to 'migration'
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.database", havingValue = "migrate")
public class DataMigrationRunner implements ApplicationRunner {

    private final DataMigrationService migrationService;

    @Autowired
    public DataMigrationRunner(DataMigrationService migrationService) {
        this.migrationService = migrationService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        log.info("Starting Data Migration from File Storage to DynamoDB");

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
        }
    }
}
