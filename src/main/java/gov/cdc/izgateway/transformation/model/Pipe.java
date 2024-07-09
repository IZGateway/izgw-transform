package gov.cdc.izgateway.transformation.model;

import gov.cdc.izgateway.transformation.preconditions.Precondition;
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

    @NotNull(message = "Solution ID is required")
    private UUID solutionId;

    @NotBlank(message = "Solution Version is required")
    private String solutionVersion;

    private List<Precondition> preconditions;

}
