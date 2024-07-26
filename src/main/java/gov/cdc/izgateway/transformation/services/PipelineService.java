package gov.cdc.izgateway.transformation.services;

import gov.cdc.izgateway.transformation.model.Pipeline;
import gov.cdc.izgateway.transformation.repository.TxFormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PipelineService extends GenericService<Pipeline>{
    @Autowired
    public PipelineService(TxFormRepository<Pipeline> repo) {
        super(repo);
    }

    // TODO - Should this be here?
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
