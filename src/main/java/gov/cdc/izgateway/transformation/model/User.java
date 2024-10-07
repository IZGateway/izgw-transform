package gov.cdc.izgateway.transformation.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class User implements BaseModel {
    @NotNull(message = "User ID is required")
    private UUID id;

    @NotBlank(message = "User name is required")
    private String userName;

    // The organization associated with the user - null means the user has no organization affiliation
    private UUID organizationId;
    private Boolean active;
}

