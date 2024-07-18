package gov.cdc.izgateway.transformation.model;

import gov.cdc.izgateway.transformation.configuration.OperationConfig;
import gov.cdc.izgateway.transformation.preconditions.Precondition;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SolutionOperation {
    private List<Precondition> preconditions;
    private List<OperationConfig> operationList;
}
