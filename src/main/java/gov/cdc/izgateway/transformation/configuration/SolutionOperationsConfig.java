package gov.cdc.izgateway.transformation.configuration;

import gov.cdc.izgateway.transformation.preconditions.Precondition;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
@Setter
public class SolutionOperationsConfig {
    private List<Precondition> preconditions;
    private List<OperationConfig> operationList;
}
