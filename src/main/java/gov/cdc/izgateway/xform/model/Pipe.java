package gov.cdc.izgateway.xform.model;

import gov.cdc.izgateway.xform.preconditions.Precondition;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Pipe {
    @NotNull(message = "Pipe ID is required")
    private UUID id;

    // TODO - verify that Solution exists in the system
    @NotNull(message = "Solution ID is required")
    private UUID solutionId;

    // TODO - verify that a Solution of this type exist with this version
    @NotBlank(message = "Solution Version is required")
    private String solutionVersion;

    @NotNull(message = "Preconditions List is required (can be empty)")
    @Valid
    private List<Precondition> preconditions;

}
