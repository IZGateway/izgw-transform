package gov.cdc.izgateway.xform.model;

import gov.cdc.izgateway.xform.validation.ValidOrganization;
import gov.cdc.izgateway.xform.validation.ValidOrganizations;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@DynamoDbBean
public class User extends BaseModel {
    @NotBlank(message = "User - Name is required")
    private String userName;

    private UUID id;

    @NotNull(message = "User - Organization is required")
    @ValidOrganizations(message = "Organization ID must reference an existing and active organization")
    private Set<UUID> organizationIds;

    @NotNull(message = "User - Active status is required")
    private Boolean active;

    public User() {
    }

    public User(String userName) {
        this.userName = userName;
    }
}
