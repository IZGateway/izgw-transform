package gov.cdc.izgateway.transformation.services;

import gov.cdc.izgateway.transformation.model.Pipeline;
import gov.cdc.izgateway.transformation.repository.TxFormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PipelineService extends GenericService<Pipeline>{
    @Autowired
    public PipelineService(TxFormRepository<Pipeline> repo) {
        super(repo);
    }

    public Pipeline getPipelineByOrganizationAndEndpoints(UUID organizationId, String inboundEndpoint, String outboundEndpoint) {
        return repo.getEntitySet()
                .stream()
                .filter(
                        p -> p.getOrganizationId().equals(organizationId)
                                && p.getInboundEndpoint().equals(inboundEndpoint)
                                && p.getOutboundEndpoint().equals(outboundEndpoint)
                ).findFirst().orElse(null);
    }
}
