package gov.cdc.izgateway.transformation.configuration;

import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.model.Organization;
import gov.cdc.izgateway.transformation.model.Pipeline;
import gov.cdc.izgateway.transformation.model.Solution;
import gov.cdc.izgateway.transformation.services.OrganizationService;
import gov.cdc.izgateway.transformation.services.PipelineService;
import gov.cdc.izgateway.transformation.services.SolutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TxServiceConfig {
    private final OrganizationService organizationService;
    private final PipelineService pipelineService;
    private final SolutionService solutionService;

    @Autowired
    public TxServiceConfig(OrganizationService organizationService, PipelineService pipelineService, SolutionService solutionService) {
        this.pipelineService = pipelineService;
        this.solutionService = solutionService;
        this.organizationService = organizationService;
    }

    public Organization getOrganization(UUID organizationId) {
        ResponseEntity<Organization> organization = organizationService.getObject(organizationId);
        return organization.getBody();
    }

    public Pipeline getPipeline(UUID pipelineId) {
        ResponseEntity<Pipeline> pipeline = pipelineService.getObject(pipelineId);
        return pipeline.getBody();
    }

    public Pipeline findPipelineByContext(ServiceContext context) {
        return pipelineService.getPipelineByOrganizationAndEndpoints(context.getOrganizationId(), context.getInboundEndpoint(), context.getOutboundEndpoint());
    }

    public Solution getSolution(UUID solutionId) {
        ResponseEntity<Solution> solution = solutionService.getObject(solutionId);
        return solution.getBody();
    }
}
