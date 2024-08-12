package gov.cdc.izgateway.transformation.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Facility implements BaseModel {
    @NotBlank(message = "Facility name is required")
    private String facilityName;
    @NotNull(message = "Facility Unique ID is required")
    private UUID id;
    @NotNull(message = "Facility active status is required")
    private Boolean active;

    @NotNull(message = "Facility Identifier is required")
    private String facilityIdentifier;

    @NotNull(message = "Organization ID is required")
    private UUID organizationId;
}
