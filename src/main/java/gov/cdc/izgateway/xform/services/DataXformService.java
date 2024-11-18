package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.xform.context.IZGXformContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DataXformService {
    private final PipelineRunnerService pipelineRunnerService;

    @Autowired
    public DataXformService(PipelineRunnerService pipelineRunnerService) {
        this.pipelineRunnerService = pipelineRunnerService;
    }

    public IZGXformContext transform(IZGXformContext context) throws Exception {
        pipelineRunnerService.execute(context.getServiceContext());
        return context;
    }
}
