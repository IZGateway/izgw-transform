package gov.cdc.izgateway.xform.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Solution implements BaseModel {

    private UUID id;

    @NotBlank(message = "Solution name is required")
    private String solutionName;

    private String description;

    @NotBlank(message = "Solution version is required")
    private String version;

    /**
     * FYI - Jackson will do things like the number 1 sets to true.
     * The number 0 sets to false.
     * But also any non-zero number sets to true.
     */
    @NotNull(message = "Solution active status is required")
    private Boolean active;

    @NotNull(message = "Request Operations List is required (can be empty)")
    @Valid
    private List<SolutionOperation> requestOperations;

    @NotNull(message = "Response Operations List is required (can be empty)")
    @Valid
    private List<SolutionOperation> responseOperations;

}
