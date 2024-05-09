package gov.cdc.izgateway.transformation.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Getter
@Setter
public class OrganizationConfig {
    private String organizationName;
    private UUID organizationId;
    private List<PipelineConfig> pipelines;
}
