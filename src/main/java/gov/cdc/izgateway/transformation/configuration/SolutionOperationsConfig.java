package gov.cdc.izgateway.transformation.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
@Setter
public class SolutionOperationsConfig {
    private List<OperationConfig> preconditions;
    private List<OperationConfig> operationList;
}
