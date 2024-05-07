package gov.cdc.izgateway.transformation.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
@Setter
public class OrganizationConfig {
    private String organizationName;
    private List<PipelineConfig> pipelines;
}
