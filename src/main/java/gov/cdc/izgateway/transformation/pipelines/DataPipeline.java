package gov.cdc.izgateway.transformation.pipelines;

import gov.cdc.izgateway.transformation.configuration.TxServiceConfig;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.logging.advice.Advisable;
import gov.cdc.izgateway.transformation.model.Pipe;
import gov.cdc.izgateway.transformation.model.Pipeline;
import gov.cdc.izgateway.transformation.model.Solution;
import gov.cdc.izgateway.transformation.solutions.SolutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// TODO - rename to PipelneRunner?  Something else.

@Service
@Slf4j
public class DataPipeline implements Advisable {
    private final TxServiceConfig txServiceConfig;
    private Pipeline pipeline;

    @Autowired
    public DataPipeline(TxServiceConfig txServiceConfig) {
        this.txServiceConfig = txServiceConfig;
    }

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
            Solution solutionModel = txServiceConfig.getSolution(pipe.getSolutionId());

            SolutionService solutionService = new SolutionService(solutionModel, context.getDataType());

            solutionService.execute(context);

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
