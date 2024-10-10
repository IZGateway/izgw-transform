package gov.cdc.izgateway.transformation.services;

import gov.cdc.izgateway.transformation.annotations.CaptureXformAdvice;
import gov.cdc.izgateway.transformation.configuration.TxServiceConfig;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.logging.advice.Advisable;
import gov.cdc.izgateway.transformation.logging.advice.Transformable;
import gov.cdc.izgateway.transformation.model.Pipe;
import gov.cdc.izgateway.transformation.model.Pipeline;
import gov.cdc.izgateway.transformation.preconditions.*;
import gov.cdc.izgateway.transformation.solutions.Solution;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@Getter
public class PipelineRunnerService implements Advisable, Transformable {
    private final TxServiceConfig txServiceConfig;
    private ServiceContext context;
    private Pipeline pipeline;
    private UUID id;

    @Autowired
    public PipelineRunnerService(TxServiceConfig txServiceConfig) {
        this.txServiceConfig = txServiceConfig;
    }

    @CaptureXformAdvice
    public void execute(ServiceContext context) throws Exception {
        pipeline = txServiceConfig.findPipelineByContext(context);
        this.context = context;

        if (pipeline != null) {
            log.trace("Executing Pipeline ({}) '{}'", pipeline.getId(), pipeline.getPipelineName());
            id = pipeline.getId();
        } else {
            log.trace("No Pipeline Found");
        }

        for (Pipe pipe : pipeline.getPipes()) {
            log.trace("Executing Pipe: {}", pipe.getId());

            if (Boolean.TRUE.equals(preconditionsPassed(pipe))) {
                // Create & Execute Solution
                gov.cdc.izgateway.transformation.model.Solution solutionModel = txServiceConfig.getSolution(pipe.getSolutionId());
                log.trace("Solution Name: {}", solutionModel.getSolutionName());
                Solution solution = new Solution(solutionModel);
                solution.execute(context);
            }

        }

    }

    private Boolean preconditionsPassed(Pipe pipe) {
        boolean passed = true;

        for (Precondition op : pipe.getPreconditions()) {
            passed = passed && op.evaluate(context);
        }

        return passed;

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
    public boolean hasTransformed() {
        // We won't know until we execute the pipeline.
        return false;
    }
}
