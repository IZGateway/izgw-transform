package gov.cdc.izgateway.transformation.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Getter
@Setter
public class PipeConfig {
    private List<OperationConfig> preconditions;
    private UUID solutionId;
    private String solutionName;
    private String version;
}
