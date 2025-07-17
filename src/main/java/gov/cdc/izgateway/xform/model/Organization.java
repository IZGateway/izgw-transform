package gov.cdc.izgateway.xform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import java.util.UUID;

@Getter
@Setter
@DynamoDbBean
public class Organization extends BaseModel implements OrganizationAware {
    @NotBlank(message = "Organization name is required")
    private String organizationName;

    private UUID id;

    @NotNull(message = "Organization active status is required")
    private Boolean active;

    @NotNull(message = "Organization common name is required")
    private String commonName;

    @JsonIgnore
    public UUID getOrganizationId() {
        return id;
    }
}
