package gov.cdc.izgateway.xform.repository.dynamodb.migration;

import gov.cdc.izgateway.configuration.DynamoDbConfig;
import gov.cdc.izgateway.xform.model.BaseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Factory that creates EntityMigrator instances for all configured entity types.
 * This eliminates the need for individual migrator @Component classes.
 */
@Slf4j
@Configuration
@ConditionalOnExpression("'${spring.database:}'.equalsIgnoreCase('migrate') || '${spring.database:}'.equalsIgnoreCase('reinit')")
public class MigratorFactory {

    private final DynamoDbEnhancedClient dynamoDbClient;
    private final String tableName;
    private final MigrationConfiguration migrationConfig;

    public MigratorFactory(DynamoDbEnhancedClient dynamoDbClient,
                           DynamoDbConfig dynamoDbConfig,
                           MigrationConfiguration migrationConfig) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = dynamoDbConfig.getDynamodbTable();
        this.migrationConfig = migrationConfig;
    }

    /**
     * Creates EntityMigrator instances for all configured entity types.
     * Spring will automatically inject this list into DataMigrationService.
     */
    @Bean
    public List<EntityMigrator<?>> entityMigrators() {
        return migrationConfig.getEntityConfigurations()
                .entrySet()
                .stream()
                .map(entry -> createMigrator(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Creates a generic migrator for the specified entity type.
     */
    @SuppressWarnings("unchecked")
    private <T extends BaseModel> EntityMigrator<T> createMigrator(Class<T> entityClass, String filePath) {
        log.info("Creating migrator for {} with file path: {}", entityClass.getSimpleName(), filePath);
        return new GenericEntityMigrator<>(entityClass, filePath, dynamoDbClient, tableName);
    }
}
