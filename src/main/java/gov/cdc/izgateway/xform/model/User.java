package gov.cdc.izgateway.xform.model;

import gov.cdc.izgateway.xform.validation.ValidOrganization;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class User implements BaseModel {
    @NotBlank(message = "User - Name is required")
    private String userName;

    @NotNull(message = "User - ID is required")
    private UUID id;

    @NotNull(message = "User - Organization is required")
    @ValidOrganization(message = "Organization ID must reference an existing and active organization")
    private UUID organizationId;

    @NotNull(message = "User - Active status is required")
    private Boolean active;

    public User() {
    }

    public User(String userName) {
        this.userName = userName;
    }
}
