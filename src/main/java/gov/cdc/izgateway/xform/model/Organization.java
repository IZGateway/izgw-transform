package gov.cdc.izgateway.xform.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.UUID;

@Getter
@Setter
@DynamoDbBean
public class Organization implements BaseModel {
    @NotBlank(message = "Organization name is required")
    private String organizationName;
    
    private UUID id;
    
    @NotNull(message = "Organization active status is required")
    private Boolean active;
    
    @NotNull(message = "Organization common name is required")
    private String commonName;

    @JsonIgnore
    @DynamoDbPartitionKey
    @DynamoDbAttribute("entityType")
    public String getEntityType() {
        // TODO - get this via reflection?
        return "Organization";
    }
    
    public void setEntityType(String entityType) {
        // Read-only attribute
    }

    @JsonIgnore
    @DynamoDbSortKey
    @DynamoDbAttribute("sortKey")
    public String getSortKey() {
        return id != null ? id.toString() : null;
    }
    
    public void setSortKey(String sortKey) {
        this.id = sortKey != null ? UUID.fromString(sortKey) : null;
    }
}

