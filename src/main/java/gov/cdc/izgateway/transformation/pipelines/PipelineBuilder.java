package gov.cdc.izgateway.transformation.pipelines;

import gov.cdc.izgateway.transformation.configuration.*;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.operations.Hl7v2EmptyOperation;
import gov.cdc.izgateway.transformation.operations.Hl7v2EqualsOperation;
import gov.cdc.izgateway.transformation.operations.Hl7v2NotEmptyOperation;
import gov.cdc.izgateway.transformation.operations.Hl7v2NotEqualsOperation;
import gov.cdc.izgateway.transformation.pipes.Hl7v2Pipe;
import gov.cdc.izgateway.transformation.solutions.Solution;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Log
public class PipelineBuilder {

    public Hl7Pipeline build(ServiceContext context) throws Exception {

        Hl7Pipeline pipeline = new Hl7Pipeline();

        // TODO - clean this up, quick/dirty obviously can be better

        // Get pipeline from the context
        // Context has Organzation, Inbound Endpoint & Outbound Endpoint
        // Should only be 1 Pipeline for that combination of information
        PipelineConfig pipelineConfig = context.getPipeline();

        // build chain
        if (pipelineConfig != null) {
            // TODO - make generic isn't necessarily always going to be an Hl7Pipeline we are building here.
            pipeline = new Hl7Pipeline(pipelineConfig);

            // A pipeline will have "pipes"
            for (PipeConfig pipeConfig : pipelineConfig.getPipes()) {
                Hl7v2Pipe pipe = new Hl7v2Pipe();

                for (OperationConfig co : pipeConfig.getPreconditions()) {
                    // Precondition
                    // TODO - this is repeated in SolutionOperation.java fix this
                    if (co instanceof ConditionEqualsConfig conditionEqualsConfig) {
                        pipe.addPrecondition(new Hl7v2EqualsOperation(conditionEqualsConfig));
                    } else if (co instanceof ConditionNotEqualsConfig conditionNotEqualsConfig) {
                        pipe.addPrecondition(new Hl7v2NotEqualsOperation(conditionNotEqualsConfig));
                    } else if (co instanceof ConditionNotEmptyConfig conditionNotEmptyConfig) {
                        pipe.addPrecondition(new Hl7v2NotEmptyOperation(conditionNotEmptyConfig));
                    } else if (co instanceof ConditionEmptyConfig conditionEmptyConfig) {
                        pipe.addPrecondition(new Hl7v2EmptyOperation(conditionEmptyConfig));
                    }
                }

                // Get Solution configuration from full system configuration
                // TODO - take version into account!
                Optional<SolutionConfig> solutionConfig = context.getConfiguration().getSolutions().stream()
                        .filter(sc -> sc.getId().equals(pipeConfig.getSolutionId()))
                        .findFirst();

                if (solutionConfig.isPresent()) {
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