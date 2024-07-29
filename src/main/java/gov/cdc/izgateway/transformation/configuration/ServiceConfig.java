package gov.cdc.izgateway.transformation.configuration;

import gov.cdc.izgateway.transformation.context.ServiceContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
@Getter
@Setter
public class ServiceConfig {
    private List<OrganizationConfig> organizations;
    private List<SolutionConfig> solutions;

    public OrganizationConfig getOrganizationConfig(ServiceContext context) {
        return getOrganizations()
                .stream()
                .filter(org -> Objects.equals(org.getOrganizationId(), context.getOrganizationId()))
                .reduce((a, b) -> {
                    throw new IllegalStateException("More than one OrganizationConfig found for id " + context.getOrganizationId());
                }).orElse(null);
    }

    public PipelineConfig getPipelineConfig(ServiceContext context) {
        return getOrganizationConfig(context).getPipelines()
                .stream()
                .filter(
                        pl -> Objects.equals(pl.getInboundEndpoint(), context.getInboundEndpoint()) &&
                        	  Objects.equals(pl.getOutboundEndpoint(),context.getOutboundEndpoint())
                )
                .reduce((a, b) -> {
                    throw new IllegalStateException(String.format("More than one PipelineConfig found for Organization ID '%s', Inbound Endpoint '%s', and Outbound Endpoint '%s'",
                            context.getOrganizationId(),
                            context.getInboundEndpoint(),
                            context.getOutboundEndpoint()));
                }).orElse(null);
    }

    public Optional<SolutionConfig> getSolutionConfigById(UUID solutionId) {
        return getSolutions().stream()
                .filter(sc -> Objects.equals(sc.getId(), solutionId))
                .findFirst();
    }
}
