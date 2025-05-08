package gov.cdc.izgateway.xform.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Mapping implements BaseModel, OrganizationAware {
    private UUID id;
    @NotNull(message = "Mapping active status is required")
    private Boolean active;
    @NotNull(message = "Organization ID is required")
    private UUID organizationId;
    @NotNull(message = "Code system is required")
    private String codeSystem;
    @NotNull(message = "Code is required")
    private String code;
    @NotNull(message = "Target code system is required")
    private String targetCodeSystem;
    @NotNull(message = "Target code is required")
    private String targetCode;
}
