package gov.cdc.izgateway.xform.model;

import gov.cdc.izgateway.xform.validation.ValidUser;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@DynamoDbBean
public class AccessControl extends BaseModel {
    private UUID id;
    @NotNull(message = "Access Control - User is required")
    @ValidUser(message = "User ID must reference an existing and active user")
    private UUID userId;
    @NotNull(message = "Access Control - Active status is required")
    private Boolean active;
    @NotNull(message = "Access Control - Roles are required")
    private List<String> roles;
}
