package gov.cdc.izgateway.xform.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class User implements BaseModel {
    @NotNull(message = "User ID is required")
    private UUID id;

    @NotBlank(message = "User name is required")
    private String userName;

    private Boolean active;
}
