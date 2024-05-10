package gov.cdc.izgateway.transformation.chains;

import gov.cdc.izgateway.transformation.configuration.OrganizationConfig;
import gov.cdc.izgateway.transformation.configuration.PipelineConfig;
import gov.cdc.izgateway.transformation.configuration.ServiceConfig;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.pipelines.Hl7Pipeline;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Log
public class PipelineChainBuilder {
    @Autowired
    ServiceConfig serviceConfig;

    public PipelineChain build(ServiceContext context) {
        PipelineChain chain = new PipelineChain();

        // TODO - clean this up, quick/dirty obviously can be better

        // get Organization objects for the context org
        List<OrganizationConfig> organizations = serviceConfig
                .getOrganizations()
                .stream()
                .filter(
                        org -> org.getOrganizationId().equals(context.getOrganizationId())
                )
                .toList();

        // get pipelines for the context ib and ob endpoints
        List<PipelineConfig> pipelineConfigs = new ArrayList<>();
        for (OrganizationConfig organizationConfig : organizations) {
            pipelineConfigs = organizationConfig.getPipelines().stream()
                    .filter(
                            pipeline -> pipeline.getInboundEndpoint().equals(context.getInboundEndpoint()) && pipeline.getOutboundEndpoint().equals(context.getOutboundEndpoint())
                    )
                    .toList();
        }

        // TODO - I think there should only be 1 Pipeline per Org / IB / OB
        //        need to verify and potentially fail here?  or something?

        // build chain
        for (PipelineConfig pipelineConfig : pipelineConfigs) {
            // TODO - make generic isn't necessarily always going to be an Hl7Pipeline we are building here.
            chain.addPipeline(new Hl7Pipeline(pipelineConfig));
        }

        return chain;
    }
}
