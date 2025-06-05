package gov.cdc.izgateway.xform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cdc.izgateway.xform.preconditions.Precondition;
import gov.cdc.izgateway.xform.validation.ValidSolution;
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
@ValidSolution
@DynamoDbBean
public class Pipe {
    @NotNull(message = "Pipe ID is required")
    private UUID id;

    @NotNull(message = "Solution ID is required")
    private UUID solutionId;

    @NotBlank(message = "Solution Version is required")
    private String solutionVersion;

    @NotNull(message = "Preconditions List is required (can be empty)")
    @Valid
    private List<Precondition> preconditions;

    /*
     * DynamoDB only has built-in conversion for primitives (String, Integer, etc...)
     * and simple collections (List<String> for example).
     * Since Precondition is an interface, with multiple implementations plus it does not
     * understand Jackson's @JsonSubTypes we need to create these getter/setters.
     */

    @DynamoDbIgnore
    public List<Precondition> getPreconditions() {
        return preconditions;
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @JsonIgnore
    @DynamoDbAttribute("preconditions")
    public String getPreconditionsJson() {
        if (preconditions == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(preconditions);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing preconditions", e);
        }
    }

    public void setPreconditionsJson(String preconditionsJson) {
        if (preconditionsJson == null) {
            this.preconditions = null;
            return;
        }
        try {
            this.preconditions = objectMapper.readValue(preconditionsJson, new TypeReference<List<Precondition>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing preconditions", e);
        }
    }
}
