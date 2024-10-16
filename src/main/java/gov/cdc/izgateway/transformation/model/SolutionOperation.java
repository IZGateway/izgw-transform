package gov.cdc.izgateway.transformation.model;

import gov.cdc.izgateway.transformation.operations.Operation;
import gov.cdc.izgateway.transformation.preconditions.Precondition;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SolutionOperation {
    @NotNull(message = "Preconditions List is required (can be empty)")
    private List<Precondition> preconditions;
    @NotNull(message = "Operation List is required (can be empty)")
    private List<Operation> operationList;
}
