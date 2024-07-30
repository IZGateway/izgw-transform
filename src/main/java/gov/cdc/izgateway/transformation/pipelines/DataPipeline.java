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

@Service
@Slf4j
public class DataPipeline implements Advisable {
    private final TxServiceConfig txServiceConfig;


    @Autowired
    public DataPipeline(TxServiceConfig txServiceConfig) {
        this.txServiceConfig = txServiceConfig;
    }

    public void execute(ServiceContext context) throws Exception {
        Pipeline pipeline = txServiceConfig.findPipelineByContext(context);

        if (pipeline == null) {
            // TODO - Fail or log?
            throw new Exception(String.format("Pipeline not found for Organization ID %s", context.getOrganizationId()));
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
        //return configuration.getPipelineName();
        return "NAME - FIXME";
    }

    @Override
    public String getId() {
        //return configuration.getId().toString();
        return "ID - FIXME";
    }

    @Override
    public boolean hasTransformed() {
        // TODO - think about this after getting more familiar w/ Advisable
        return false;
    }
}
