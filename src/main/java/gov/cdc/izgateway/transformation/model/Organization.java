package gov.cdc.izgateway.transformation.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class Organization implements BaseModel {
    @NotBlank(message = "Organization name is required")
    private String name;
    @NotNull(message = "Organization ID is required")
    private UUID id;
    @NotNull(message = "Organization active status is required")
    private Boolean active;
}

