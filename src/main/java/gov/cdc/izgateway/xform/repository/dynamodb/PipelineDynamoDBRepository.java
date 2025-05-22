package gov.cdc.izgateway.xform.repository.dynamodb;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.cdc.izgateway.xform.model.Pipeline;
import gov.cdc.izgateway.xform.model.Pipe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;

import java.util.List;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "xform.repository.type", havingValue = "dynamodb")
@Primary
public class PipelineDynamoDBRepository extends GenericDynamoDBRepository<Pipeline> {

    public PipelineDynamoDBRepository(
            DynamoDbEnhancedClient dynamoDbClient,
            @Value("${xform.repository.dynamodb.table}") String tableName) {
        super(dynamoDbClient, tableName, Pipeline.class, getTableSchema());
    }

    @Override
    protected String getEntityName() {
        return "Pipeline";
    }

    private static TableSchema<Pipeline> getTableSchema() {
        return StaticTableSchema.builder(Pipeline.class)
                .newItemSupplier(Pipeline::new)
                .addAttribute(String.class, a -> a.name("entityType")
                        .getter(pipeline -> "Pipeline")
                        .setter((pipeline, val) -> {/* Read-only attribute */})
                        .tags(StaticAttributeTags.primaryPartitionKey()))
                .addAttribute(String.class, a -> a.name("sortKey")
                        .getter(pipeline -> pipeline.getId() != null ? pipeline.getId().toString() : null)
                        .setter((pipeline, val) -> pipeline.setId(val != null ? UUID.fromString(val) : null))
                        .tags(StaticAttributeTags.primarySortKey()))
                .addAttribute(String.class, a -> a.name("pipelineName")
                        .getter(Pipeline::getPipelineName)
                        .setter(Pipeline::setPipelineName))
                .addAttribute(String.class, a -> a.name("organizationId")
                        .getter(pipeline -> pipeline.getOrganizationId() != null ? pipeline.getOrganizationId().toString() : null)
                        .setter((pipeline, val) -> pipeline.setOrganizationId(val != null ? UUID.fromString(val) : null)))
                .addAttribute(String.class, a -> a.name("description")
                        .getter(Pipeline::getDescription)
                        .setter(Pipeline::setDescription))
                .addAttribute(String.class, a -> a.name("inboundEndpoint")
                        .getter(Pipeline::getInboundEndpoint)
                        .setter(Pipeline::setInboundEndpoint))
                .addAttribute(String.class, a -> a.name("outboundEndpoint")
                        .getter(Pipeline::getOutboundEndpoint)
                        .setter(Pipeline::setOutboundEndpoint))
                .addAttribute(Boolean.class, a -> a.name("active")
                        .getter(Pipeline::getActive)
                        .setter(Pipeline::setActive))
                .addAttribute(String.class, a -> a.name("pipes")
                        .getter(pipeline -> {
                            if (pipeline.getPipes() == null) {
                                return null;
                            }
                            try {
                                return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(pipeline.getPipes());
                            } catch (Exception e) {
                                throw new RuntimeException("Error serializing pipes", e);
                            }
                        })
                        .setter((pipeline, val) -> {
                            if (val == null) {
                                pipeline.setPipes(null);
                                return;
                            }
                            try {
                                pipeline.setPipes(new com.fasterxml.jackson.databind.ObjectMapper().readValue(val, 
                                    new com.fasterxml.jackson.core.type.TypeReference<List<Pipe>>() {}));
                            } catch (Exception e) {
                                throw new RuntimeException("Error deserializing pipes", e);
                            }
                        }))
                .build();
    }
}
