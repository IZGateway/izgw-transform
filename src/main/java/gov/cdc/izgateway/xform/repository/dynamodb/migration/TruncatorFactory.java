package gov.cdc.izgateway.xform.repository.dynamodb.migration;

import gov.cdc.izgateway.configuration.DynamoDbConfig;
import gov.cdc.izgateway.xform.model.BaseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Factory for creating EntityTruncator beans for all configured entity types.
 * Used to provide truncators for each entity to support table reinitialization.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.database", havingValue = "reinit")
public class TruncatorFactory {

    private final DynamoDbEnhancedClient dynamoDbClient;
    private final String tableName;
    private final MigrationConfiguration migrationConfig;

    /**
     * Constructs a TruncatorFactory with the required dependencies.
     * @param dynamoDbClient the DynamoDB enhanced client
     * @param dynamoDbConfig the DynamoDB configuration
     * @param migrationConfig the migration configuration
     */
    public TruncatorFactory(DynamoDbEnhancedClient dynamoDbClient,
                            DynamoDbConfig dynamoDbConfig,
                            MigrationConfiguration migrationConfig) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = dynamoDbConfig.getDynamodbTable();
        this.migrationConfig = migrationConfig;
    }

    /**
     * Provides a list of EntityTruncator beans, one for each configured entity type.
     * @return list of EntityTruncator instances
     */
    @Bean
    public List<EntityTruncator<?>> entityTruncators() {
        return migrationConfig.getEntityConfigurations()
                .entrySet()
                .stream()
                .map(entry -> createTruncator(entry.getKey()))
                .collect(Collectors.toList());
    }

    /**
     * Creates an EntityTruncator for the specified entity class.
     * @param entityClass the entity class
     * @return an EntityTruncator for the entity type
     */
    @SuppressWarnings("unchecked")
    private <T extends BaseModel> EntityTruncator<T> createTruncator(Class<T> entityClass) {
        log.info("Creating truncator for {}", entityClass.getSimpleName());
        return new GenericEntityTruncator<>(entityClass, dynamoDbClient, tableName);
    }
}