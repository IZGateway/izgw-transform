package gov.cdc.izgateway.transformation.configuration;

import gov.cdc.izgateway.transformation.preconditions.Precondition;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Getter
@Setter
public class PipeConfig {
    private UUID id;
    private List<Precondition> preconditions;
    private UUID solutionId;
    private String solutionName;
    private String solutionVersion;
}
