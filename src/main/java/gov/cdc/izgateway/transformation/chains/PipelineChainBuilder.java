package gov.cdc.izgateway.transformation.chains;

import gov.cdc.izgateway.transformation.configuration.*;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.pipelines.Hl7Pipeline;
import gov.cdc.izgateway.transformation.pipes.Hl7v2Pipe;
import gov.cdc.izgateway.transformation.transformers.Hl7DataTransformation;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// TODO - move building the WHOLE chain here since we have the context here.

@Service
@Log
public class PipelineChainBuilder {

    public PipelineChain build(ServiceContext context) throws Exception {
        PipelineChain chain = new PipelineChain();

        // TODO - clean this up, quick/dirty obviously can be better

        // get Organization objects for the context org
        List<OrganizationConfig> organizations = context.getConfiguration()
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
            Hl7Pipeline pipeline = new Hl7Pipeline(pipelineConfig);

            List<Hl7DataTransformation> requestTransformations;
            List<Hl7DataTransformation> responseTransformation;
            List<Hl7v2Pipe> pipes;

//            // A pipeline will have "pipes"
            pipes = new ArrayList<>();
            for (PipeConfig pipeConfig : pipelineConfig.getPipes()) {
                Hl7v2Pipe pipe = new Hl7v2Pipe(pipeConfig);

            }
            pipeline.setPipes(pipes);

            // So under single pipeline we will have Request & Response Transformations
            // Loop those in the pipeline config and build the objects
            requestTransformations = new ArrayList<>();
            for (DataTransformationConfig dtConfig : pipelineConfig.getRequestTransformations()) {
                requestTransformations.add(new Hl7DataTransformation(dtConfig));
            }
            pipeline.setRequestTransformations(requestTransformations);

            responseTransformation = new ArrayList<>();
            for (DataTransformationConfig dtConfig : pipelineConfig.getResponseTransformations()) {
                responseTransformation.add(new Hl7DataTransformation(dtConfig));
            }
            pipeline.setResponseTransformation(responseTransformation);

            chain.addPipeline(pipeline);
        }

        return chain;
    }
}
