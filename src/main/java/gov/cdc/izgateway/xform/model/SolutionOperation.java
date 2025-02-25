package gov.cdc.izgateway.xform.model;

import gov.cdc.izgateway.xform.operations.Operation;
import gov.cdc.izgateway.xform.preconditions.Precondition;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SolutionOperation {
    @NotNull(message = "Preconditions List is required (can be empty)")
    @Valid
    private List<Precondition> preconditions;
    @NotNull(message = "Operation List is required (can be empty)")
    @Valid
    private List<Operation> operationList;
}
