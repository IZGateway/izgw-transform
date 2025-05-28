package gov.cdc.izgateway.xform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cdc.izgateway.xform.validation.ValidOrganization;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@DynamoDbBean
public class Pipeline extends BaseModel implements OrganizationAware {

    @NotBlank(message = "Pipeline name is required")
    private String pipelineName;

    private UUID id;

    @NotNull(message = "Organization ID is required")
    @ValidOrganization(message = "Organization ID must reference an existing and active organization")
    private UUID organizationId;

    private String description;

    @NotBlank(message = "Inbound endpoint is required")
    private String inboundEndpoint;

    @NotBlank(message = "Outbound endpoint is required")
    private String outboundEndpoint;

    @NotNull(message = "Pipeline active status is required")
    private Boolean active;

    @NotNull(message = "Pipes List is required (can be empty)")
    @Valid
    private List<Pipe> pipes;

    /*
     * DynamoDB only has built-in conversion for primitives (String, Integer, etc...)
     * and simple collections (List<String> for example).
     * Needed to create the getter/setter to work
     */

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @JsonIgnore
    @DynamoDbAttribute("pipes")
    public String getPipesJson() {
        if (pipes == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(pipes);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing pipes", e);
        }
    }

    public void setPipesJson(String pipesJson) {
        if (pipesJson == null) {
            this.pipes = null;
            return;
        }
        try {
            this.pipes = objectMapper.readValue(pipesJson, new TypeReference<List<Pipe>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing pipes", e);
        }
    }

    @DynamoDbIgnore
    public List<Pipe> getPipes() {
        return pipes;
    }

}
