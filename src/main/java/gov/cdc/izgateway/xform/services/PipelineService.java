package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.xform.model.Pipeline;
import gov.cdc.izgateway.xform.repository.RepositoryFactory;

import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

/**
 * Service for the pipelines.
 */
@Service
public class PipelineService extends GenericService<Pipeline>{

    /**
     * Creates the service.
     *
     * @param repositoryFactory the factory that supplies the pipeline repository
     */
    @Autowired
    public PipelineService(RepositoryFactory repositoryFactory) {
        super(repositoryFactory.pipelineRepository());
    }

    /**
     * Finds the active pipeline for an organization and endpoint pair.
     *
     * @param organizationId the id of the organization
     * @param inboundEndpoint the inbound endpoint
     * @param outboundEndpoint the outbound endpoint
     * @return the pipeline, or null if no active pipeline matches
     */
    public Pipeline getPipelineByOrganizationAndEndpoints(UUID organizationId, String inboundEndpoint, String outboundEndpoint) {
        return getList()
                .stream()
                .filter(
                        p -> p.getOrganizationId().equals(organizationId)
                                && p.getInboundEndpoint().equals(inboundEndpoint)
                                && p.getOutboundEndpoint().equals(outboundEndpoint)
                                && Boolean.TRUE.equals(p.getActive())
                ).findFirst().orElse(null);
    }

    @Override
    protected boolean isDuplicate(Pipeline pipeline) {
        return isPipelineNameDuplicate(pipeline) || isEndpointDuplicate(pipeline);
    }

    /**
     * Checks if the organization of a pipeline already has a pipeline with the same name, ignoring case.
     *
     * @param pipeline the pipeline to check
     * @return true if the name is already in use
     */
    protected boolean isPipelineNameDuplicate(Pipeline pipeline) {
        return repo.getEntitySet().stream().anyMatch(p ->
                p.getOrganizationId().equals(pipeline.getOrganizationId()) &&
                        Strings.CI.equals(p.getPipelineName(), pipeline.getPipelineName())
        );
    }

    /**
     * Checks if the organization of a pipeline already has a pipeline with the same endpoints, ignoring case.
     *
     * @param pipeline the pipeline to check
     * @return true if the endpoints are already in use
     */
    protected boolean isEndpointDuplicate(Pipeline pipeline) {
        return repo.getEntitySet().stream().anyMatch(p ->
                p.getOrganizationId().equals(pipeline.getOrganizationId()) &&
                        Strings.CI.equals(p.getInboundEndpoint(), pipeline.getInboundEndpoint()) &&
                        Strings.CI.equals(p.getOutboundEndpoint(), pipeline.getOutboundEndpoint())
        );
    }
    
    /**
     * Determines if a solution is in use by any pipeline
     * @param solutionId	The solution UUID to check
     * @return			True if the solution is in use, false otherwise
     */
    public boolean isSolutionInUse(UUID solutionId) {
        return repo.getEntitySet().stream()
                .flatMap(p -> p.getPipes().stream())
                .filter(pipe -> pipe.getSolutionId().equals(solutionId))
                .findFirst()
                .orElse(null) != null;
    }

}
