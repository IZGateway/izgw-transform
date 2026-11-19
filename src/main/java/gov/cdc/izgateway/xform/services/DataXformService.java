package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.xform.context.IZGXformContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Runs the transformation pipeline for a message.
 */
@Service
@Slf4j
public class DataXformService {
    private final PipelineRunnerService pipelineRunnerService;

    /**
     * Creates the service.
     *
     * @param pipelineRunnerService the service that runs the pipeline
     */
    @Autowired
    public DataXformService(PipelineRunnerService pipelineRunnerService) {
        this.pipelineRunnerService = pipelineRunnerService;
    }

    /**
     * Runs the pipeline on the service context of the message.
     *
     * @param context the transformation context
     * @return the same context, after the pipeline ran
     * @throws Exception if the pipeline fails
     */
    public IZGXformContext transform(IZGXformContext context) throws Exception {
        pipelineRunnerService.execute(context.getServiceContext());
        return context;
    }
}
