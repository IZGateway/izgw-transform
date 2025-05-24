package gov.cdc.izgateway.xform.repository.dynamodb;

import gov.cdc.izgateway.xform.model.BaseModel;
import gov.cdc.izgateway.xform.model.Pipeline;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;

import java.util.UUID;

@Repository
@ConditionalOnExpression("'${xform.repository.type}'.equals('dynamodb') || '${xform.repository.type}'.equals('migration')")
public class PipelineRepository extends GenericDynamoDBRepository<Pipeline> {

    public PipelineRepository(
            DynamoDbEnhancedClient dynamoDbClient,
            @Value("${xform.repository.dynamodb.table}") String tableName) {
        super(dynamoDbClient, tableName, Pipeline.class, getTableSchema());
    }

    @Override
    protected String getEntityName() {
        return Pipeline.class.getSimpleName();
    }

    // TODO - look at getting rid of this somehow.
    // Can't use TableSchema.fromBean(Pipeline.class), similar to Organization
    // because Pipelines are made up of Pipes which are then made up of
    // preconditions
    private static TableSchema<Pipeline> getTableSchema() {
        return StaticTableSchema.builder(Pipeline.class)
                .newItemSupplier(Pipeline::new)
                .addAttribute(String.class, a -> a.name(BaseModel.ENTITY_TYPE)
                        .getter(pipeline -> "Pipeline")
                        .setter((pipeline, val) -> {/* Read-only attribute */})
                        .tags(StaticAttributeTags.primaryPartitionKey()))
                .addAttribute(String.class, a -> a.name(BaseModel.SORT_KEY)
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
                        .getter(Pipeline::getPipesJson)
                        .setter(Pipeline::setPipesJson))
                .build();
    }
}
