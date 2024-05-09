package gov.cdc.izgateway.transformation.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
@Setter
public class ServiceConfig {
    private List<OrganizationConfig> organizations;
    private List<SolutionConfig> solutions;
}
