package gov.cdc.izgateway.transformation.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Solution implements BaseModel {
    @NotNull(message = "Solution ID is required")
    private UUID id;

    @NotBlank(message = "Solution name is required")
    private String solutionName;

    private String description;

    @NotBlank(message = "Solution version is required")
    private String version;

    private Boolean active;


    private List<SolutionOperation> requestOperations;
    private List<SolutionOperation> responseOperations;

}
