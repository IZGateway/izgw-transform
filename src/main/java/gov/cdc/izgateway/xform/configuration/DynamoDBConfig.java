package gov.cdc.izgateway.xform.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.extensions.VersionedRecordExtension;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;

import java.net.URI;
import java.util.ServiceConfigurationError;

@Configuration
//@ConditionalOnExpression("'${spring.database:}'.equalsIgnoreCase('dynamodb') || '${spring.database:}'.equalsIgnoreCase('migrate')")
@Slf4j
public class DynamoDBConfig {

    @Value("${amazon.dynamodb.endpoint:}")
    private String dynamodbEndpoint;

    @Value("${amazon.dynamodb.table:}")
    private String dynamodbTable;

    @Bean
    public DynamoDbClient dynamoDbClient() {
        DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();
        var builder = DynamoDbClient.builder()
                .region(DefaultAwsRegionProviderChain.builder().build().getRegion())
                .credentialsProvider(credentialsProvider);

        @SuppressWarnings("unused")
        AwsCredentials credentials = credentialsProvider.resolveCredentials();

        if (StringUtils.isNotBlank(dynamodbEndpoint)) {
            builder.endpointOverride(URI.create(dynamodbEndpoint));
            log.info("DynamoDB Client initialized to {}", dynamodbEndpoint);
        }

        DynamoDbClient ddbClient = builder.build();

        ListTablesResponse listTablesResponse;
        try {
            ListTablesRequest lgtr = ListTablesRequest.builder().build();
            listTablesResponse = ddbClient.listTables(lgtr);
        } catch (Exception e) {
            log.error("Cannot list tables in DynamoDB {}", StringUtils.defaultIfEmpty(dynamodbEndpoint, "AWS"));
            throw new ServiceConfigurationError("Cannot list tables", e);
        }

        if (!listTablesResponse.hasTableNames()) {
            log.error("No tables exist in DynamoDB {}", StringUtils.defaultIfEmpty(dynamodbEndpoint, "AWS"));
            throw new ServiceConfigurationError("No tables exist in " + StringUtils.defaultIfEmpty(dynamodbEndpoint, "AWS"));
        }

        if (!listTablesResponse.tableNames().contains(dynamodbTable)) {
            log.error("Configured table does not exist in DynamoDB: {}", dynamodbTable);
            throw new ServiceConfigurationError("Configured table does not exist in DynamoDB: " + dynamodbTable);
        }

        return ddbClient;
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .extensions(VersionedRecordExtension.builder().build())
                .dynamoDbClient(dynamoDbClient)
                .build();
    }
}
