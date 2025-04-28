package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.xform.model.Pipeline;
import gov.cdc.izgateway.xform.repository.XformRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class PipelineService extends GenericService<Pipeline>{

    @Autowired
    public PipelineService(XformRepository<Pipeline> repo) {
        super(repo);
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
}
