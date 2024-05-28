package gov.cdc.izgateway.transformation.pipelines;

import gov.cdc.izgateway.transformation.enums.DataType;
import gov.cdc.izgateway.transformation.configuration.*;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.pipes.Hl7v2Pipe;
import gov.cdc.izgateway.transformation.preconditions.Equals;
import gov.cdc.izgateway.transformation.preconditions.Hl7v2Equals;
import gov.cdc.izgateway.transformation.preconditions.Precondition;
import gov.cdc.izgateway.transformation.solutions.Solution;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Log
public class PipelineBuilder {
    private final ServiceConfig serviceConfig;
private final NewPipelineBuilder newPipeline;

    @Autowired
    public PipelineBuilder(ServiceConfig serviceConfig, NewPipelineBuilder newPipeline) {
        this.serviceConfig = serviceConfig;
        this.newPipeline = newPipeline;
    }

    public Hl7Pipeline build(ServiceContext context) throws Exception {



        // TODO - pass HL7Pipeline the context and have it build itself?
        // TODO - What is best?  Putting config in Context and passing around or having each object able to read config?
        // TODO - personally I think the latter.
        // TODO - issue is getting config and context into the "class".  I'd like for the config to "just be there"
        //        but have not found a good way to do this just yet.
        Hl7Pipeline pipeline = new Hl7Pipeline();

        // TODO - clean this up, quick/dirty obviously can be better

        // Get pipeline from the context
        // Context has Organzation, Inbound Endpoint & Outbound Endpoint
        // Should only be 1 Pipeline for that combination of information
        PipelineConfig pipelineConfig = serviceConfig.getPipelineConfig(context);

        // build chain
        if (pipelineConfig != null) {
            // TODO - make generic isn't necessarily always going to be an Hl7Pipeline we are building here.
            pipeline = new Hl7Pipeline(pipelineConfig);

            // A pipeline will have "pipes"
            for (PipeConfig pipeConfig : pipelineConfig.getPipes()) {
                Hl7v2Pipe pipe = new Hl7v2Pipe();

                for (Precondition precondition : pipeConfig.getPreconditions()) {
                    if (context.getDataType().equals(DataType.HL7V2) && precondition instanceof Equals equals) {
                        pipe.addPrecondition(new Hl7v2Equals(equals));
                    }
                }

                // TODO - Hl7v2Pipe gets created with pipeConfig.  Then setSolution pulls solutionConfig
                // basically does everything internally.  Not here?

                // Get Solution configuration from full system configuration
                Optional<SolutionConfig> solutionConfig = serviceConfig.getSolutionConfigById(pipeConfig.getSolutionId());

                if (solutionConfig.isPresent()) {
                    // TODO - need to pass context here to get DataType
                    //        may require creating a SolutionBuilder???
                    pipe.setSolution(new Solution(solutionConfig.get()));
                } else {
                    throw new Exception(String.format("Solution not found in system with ID %s", pipeConfig.getSolutionId()));
                }

                pipeline.addPipe(pipe);
            }
        }

        return pipeline;
    }
}
