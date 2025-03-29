package gov.cdc.izgateway.xform.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class GroupRoleMapping implements BaseModel {
    @NotNull(message = "GroupRoleMapping - ID is required")
    private UUID id;
    @NotBlank(message = "GroupRoleMapping - Name is required")
    private String groupName;
    @NotNull(message = "GroupRoleMapping - Active status is required")
    private Boolean active;
    @NotNull(message = "GroupRoleMapping - Roles are required")
    private String[] roles;
}
