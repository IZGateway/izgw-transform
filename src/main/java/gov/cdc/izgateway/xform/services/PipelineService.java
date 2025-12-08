package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.xform.model.Pipeline;
import gov.cdc.izgateway.xform.repository.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class PipelineService extends GenericService<Pipeline>{

    @Autowired
    public PipelineService(RepositoryFactory repositoryFactory) {
        super(repositoryFactory.pipelineRepository());
    }

    public Pipeline getPipelineByOrganizationAndEndpoints(UUID organizationId, String inboundEndpoint, String outboundEndpoint) {
        return getList()
                .stream()
                .filter(
                        p -> p.getOrganizationId().equals(organizationId)
                                && p.getInboundEndpoint().equals(inboundEndpoint)
                                && p.getOutboundEndpoint().equals(outboundEndpoint)
                ).findFirst().orElse(null);
    }

    @Override
    protected boolean isDuplicate(Pipeline pipeline) {
        return repo.getEntitySet().stream().anyMatch(p ->
                p.getOrganizationId().equals(pipeline.getOrganizationId()) &&
                        p.getInboundEndpoint().equalsIgnoreCase(pipeline.getInboundEndpoint()) &&
                        p.getOutboundEndpoint().equalsIgnoreCase(pipeline.getOutboundEndpoint())
        );
    }
    
    /**
     * Determines if a solution is in use by any pipeline
     * @param solutionId	The solution UUID to check
     * @return			True if the solution is in use, false otherwise
     */
    public boolean isSolutionInUse(UUID solutionId) {
    	return repo.getEntitySet().stream()
				.flatMap(p -> p.getPipes() == null ? Stream.empty() : p.getPipes().stream())
				.filter(pipe -> pipe.getSolutionId().equals(solutionId))
				.findFirst()
				.orElse(null) != null;
	}

}
