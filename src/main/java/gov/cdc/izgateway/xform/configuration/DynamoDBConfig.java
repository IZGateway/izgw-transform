package gov.cdc.izgateway.xform.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.extensions.VersionedRecordExtension;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import java.net.URI;

@Configuration
@ConditionalOnExpression("'${xform.repository.type}'.equals('dynamodb') || '${xform.repository.type}'.equals('migration')")
@Slf4j
public class DynamoDBConfig {

    @Value("${xform.repository.dynamodb.endpoint:}")
    private String dynamodbEndpoint;

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

        // TODO - add check similar to hub to for tables

        return builder.build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .extensions(VersionedRecordExtension.builder().build())
                .dynamoDbClient(dynamoDbClient)
                .build();
    }
}
