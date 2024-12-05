package gov.cdc.izgateway.xform.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class GroupRoleMapping implements BaseModel {
    @NotNull(message = "ID is required")
    private UUID id;
    @NotNull(message = "Group name is required")
    private String groupName;
    @NotNull(message = "Group role mapping active status is required")
    private Boolean active;
    @NotNull(message = "Group role mapping roles are required")
    private String[] roles;
}
