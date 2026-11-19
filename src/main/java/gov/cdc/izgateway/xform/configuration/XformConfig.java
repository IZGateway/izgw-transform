package gov.cdc.izgateway.xform.configuration;

import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.model.Organization;
import gov.cdc.izgateway.xform.model.Pipeline;
import gov.cdc.izgateway.xform.model.Solution;
import gov.cdc.izgateway.xform.services.OrganizationService;
import gov.cdc.izgateway.xform.services.PipelineService;
import gov.cdc.izgateway.xform.services.SolutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Facade over the organization, pipeline and solution services that the transformation runtime uses to look up its configuration.
 */
@Component
public class XformConfig {
    private final OrganizationService organizationService;
    private final PipelineService pipelineService;
    private final SolutionService solutionService;

    /**
     * Creates the facade.
     *
     * @param organizationService the service for organizations
     * @param pipelineService the service for pipelines
     * @param solutionService the service for solutions
     */
    @Autowired
    public XformConfig(OrganizationService organizationService, PipelineService pipelineService, SolutionService solutionService) {
        this.pipelineService = pipelineService;
        this.solutionService = solutionService;
        this.organizationService = organizationService;
    }

    /**
     * Gets an organization by its identifier.
     *
     * @param organizationId the organization identifier
     * @return the organization
     */
    public Organization getOrganization(UUID organizationId) {
        return organizationService.getObject(organizationId);
    }

    /**
     * Gets a pipeline by its identifier.
     *
     * @param pipelineId the pipeline identifier
     * @return the pipeline
     */
    public Pipeline getPipeline(UUID pipelineId) {
        return pipelineService.getObject(pipelineId);
    }

    /**
     * Finds the pipeline for the organization, inbound endpoint and outbound endpoint of a service context.
     *
     * @param context the service context of the message
     * @return the matching pipeline
     */
    public Pipeline findPipelineByContext(ServiceContext context) {
        return pipelineService.getPipelineByOrganizationAndEndpoints(context.getOrganizationId(), context.getInboundEndpoint(), context.getOutboundEndpoint());
    }

    /**
     * Gets a solution by its identifier.
     *
     * @param solutionId the solution identifier
     * @return the solution
     */
    public Solution getSolution(UUID solutionId) {
        return solutionService.getObject(solutionId);
    }
}
