package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.xform.annotations.CaptureXformAdvice;
import gov.cdc.izgateway.xform.configuration.XformConfig;
import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.enums.DataFlowDirection;
import gov.cdc.izgateway.xform.logging.advice.Advisable;
import gov.cdc.izgateway.xform.logging.advice.Transformable;
import gov.cdc.izgateway.xform.model.Pipe;
import gov.cdc.izgateway.xform.model.Pipeline;
import gov.cdc.izgateway.xform.preconditions.Precondition;
import gov.cdc.izgateway.xform.solutions.Solution;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@Getter
public class PipelineRunnerService implements Advisable, Transformable {
    private final XformConfig xformConfig;
    private ServiceContext context;
    private Pipeline pipeline;
    private UUID id;

    @Autowired
    public PipelineRunnerService(XformConfig xformConfig) {
        this.xformConfig = xformConfig;
    }

    @CaptureXformAdvice
    public void execute(ServiceContext context) throws Exception {
        pipeline = xformConfig.findPipelineByContext(context);
        this.context = context;

        if (pipeline != null) {
            log.trace("Executing Pipeline ({}) '{}'", pipeline.getId(), pipeline.getPipelineName());
            id = pipeline.getId();
        } else {
            log.trace("No Pipeline Found");
            return;
        }

        List<Pipe> pipes = pipeline.getPipes();
        // Reverse the direction of pipes processing the response
        if (DataFlowDirection.RESPONSE.equals(context.getCurrentDirection())) {
        	pipes = new ArrayList<Pipe>(pipes);
        	Collections.reverse(pipes);
        }
        for (Pipe pipe : pipes) {
            log.trace("Executing Pipe: {}", pipe.getId());

            if (Boolean.TRUE.equals(preconditionsPassed(pipe))) {
                // Create & Execute Solution
                gov.cdc.izgateway.xform.model.Solution solutionModel = xformConfig.getSolution(pipe.getSolutionId());
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
