package gov.cdc.izgateway.transformation.services;

import gov.cdc.izgateway.transformation.annotations.CaptureXformAdvice;
import gov.cdc.izgateway.transformation.configuration.TxServiceConfig;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.logging.advice.Advisable;
import gov.cdc.izgateway.transformation.model.Pipe;
import gov.cdc.izgateway.transformation.model.Pipeline;
import gov.cdc.izgateway.transformation.solutions.Solution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PipelineRunnerService implements Advisable {
    private final TxServiceConfig txServiceConfig;
    private Pipeline pipeline;

    @Autowired
    public PipelineRunnerService(TxServiceConfig txServiceConfig) {
        this.txServiceConfig = txServiceConfig;
    }

    @CaptureXformAdvice
    public void execute(ServiceContext context) throws Exception {
        pipeline = txServiceConfig.findPipelineByContext(context);

        if (pipeline != null) {
            log.trace(String.format("Executing Pipeline (%s) '%s'", pipeline.getId(), pipeline.getPipelineName()));
        } else {
            log.trace("No Pipeline Found");
        }

        for (Pipe pipe : pipeline.getPipes()) {
            log.trace(String.format("Executing Pipe: %s", pipe.getId()));

            // Create Solution
            gov.cdc.izgateway.transformation.model.Solution solutionModel = txServiceConfig.getSolution(pipe.getSolutionId());

            Solution solution = new Solution(solutionModel, context.getDataType());

            solution.execute(context);

            log.trace(String.format("Solution Name: %s", solutionModel.getSolutionName()));

        }
    }

    @Override
    public String getName() {
        if (pipeline != null) {
            return pipeline.getPipelineName();
        } else {
            return "No Pipeline";
        }
    }

    @Override
    public String getId() {
        if (pipeline != null) {
            return pipeline.getId().toString();
        } else {
            return "No Pipeline";
        }    }

    @Override
    public boolean hasTransformed() {
        // We won't know until we execute the pipeline.
        return false;
    }
}
