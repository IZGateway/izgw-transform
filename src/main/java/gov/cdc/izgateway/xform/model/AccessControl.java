package gov.cdc.izgateway.xform.model;

import gov.cdc.izgateway.xform.validation.ValidUser;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AccessControl implements BaseModel {
    @NotNull(message = "Access Control - ID is required")
    private UUID id;
    @NotNull(message = "Access Control - User is required")
    @ValidUser(message = "User ID must reference an existing and active user")
    private UUID userId;
    @NotNull(message = "Access Control - Active status is required")
    private Boolean active;
    @NotNull(message = "Access Control - Roles are required")
    private String[] roles;
}
