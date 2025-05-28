package gov.cdc.izgateway.xform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class Solution extends BaseModel {

    private UUID id;

    @NotBlank(message = "Solution name is required")
    private String solutionName;

    private String description;

    @NotBlank(message = "Solution version is required")
    private String version;

    /**
     * FYI - Jackson will do things like the number 1 sets to true.
     * The number 0 sets to false.
     * But also any non-zero number sets to true.
     */
    @NotNull(message = "Solution active status is required")
    private Boolean active;

    @NotNull(message = "Request Operations List is required (can be empty)")
    @Valid
    private List<SolutionOperation> requestOperations;

    @NotNull(message = "Response Operations List is required (can be empty)")
    @Valid
    private List<SolutionOperation> responseOperations;

    /*
     * DynamoDB only has built-in conversion for primitives (String, Integer, etc...)
     * and simple collections (List<String> for example).
     * Needed to create the getter/setter to work
     */

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @JsonIgnore
    @DynamoDbAttribute("requestOperations")
    public String getRequestOperationsJson() {
        if (requestOperations == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(requestOperations);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing requestOperations", e);
        }
    }

    public void setRequestOperationsJson(String requestOperationsJson) {
        if (requestOperationsJson == null) {
            this.requestOperations = null;
            return;
        }
        try {
            this.requestOperations = objectMapper.readValue(requestOperationsJson, new TypeReference<List<SolutionOperation>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing requestOperations", e);
        }
    }

    @DynamoDbIgnore
    public List<SolutionOperation> getRequestOperations() {
        return requestOperations;
    }

    @JsonIgnore
    @DynamoDbAttribute("responseOperations")
    public String getResponseOperationsJson() {
        if (responseOperations == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(responseOperations);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing responseOperations", e);
        }
    }

    public void setResponseOperationsJson(String responseOperationsJson) {
        if (responseOperationsJson == null) {
            this.responseOperations = null;
            return;
        }
        try {
            this.responseOperations = objectMapper.readValue(responseOperationsJson, new TypeReference<List<SolutionOperation>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing responseOperations", e);
        }
    }

    @DynamoDbIgnore
    public List<SolutionOperation> getResponseOperations() {
        return responseOperations;
    }

}
