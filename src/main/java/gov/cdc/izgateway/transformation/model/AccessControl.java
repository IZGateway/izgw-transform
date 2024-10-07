package gov.cdc.izgateway.transformation.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.UUID;

@Getter
@Setter
public class AccessControl implements BaseModel {
    @NotNull(message = "Access Control ID is required")
    private UUID id;
    @NotNull(message = "User ID is required")
    private UUID userId;
    @NotNull(message = "Access Control active status is required")
    private Boolean active;
    @NotNull(message = "Access Control roles are required")
    private String[] roles;
}