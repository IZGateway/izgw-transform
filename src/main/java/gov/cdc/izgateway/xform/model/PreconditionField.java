package gov.cdc.izgateway.xform.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PreconditionField extends BaseModel {

    @NotNull(message = "Precondition Field ID is required")
    private UUID id;

    @NotBlank(message = "Precondition Field name is required")
    private String fieldName;

    @NotNull(message = "Precondition Field active status is required")
    private String dataPath;

    @NotNull(message = "Precondition Field active status is required")
    private Boolean active;
}
