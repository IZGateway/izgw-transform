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
    @NotBlank(message = "User name is required")
    private String userName;
    private UUID id;

    // The organization associated with the user - null means the user has no organization affiliation
    private UUID organizationId;
    private Boolean active;

    public User() {
    }

    public User(String userName) {
        this.userName = userName;
    }
}

