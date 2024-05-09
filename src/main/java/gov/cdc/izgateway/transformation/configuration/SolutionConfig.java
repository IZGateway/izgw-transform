package gov.cdc.izgateway.transformation.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Getter
@Setter
public class SolutionConfig {
    private UUID id;
    private String name;
    private String description;
    private String version;
    private List<SolutionOperationsConfig> requestOperations;
    private List<SolutionOperationsConfig> responseOperations;
}
