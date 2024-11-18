package gov.cdc.izgateway.xform.services;

import gov.cdc.izgateway.xform.context.IZGTransformationContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DataTransformerService {
    private final PipelineRunnerService pipelineRunnerService;

    @Autowired
    public DataTransformerService(PipelineRunnerService pipelineRunnerService) {
        this.pipelineRunnerService = pipelineRunnerService;
    }

    public IZGTransformationContext transform(IZGTransformationContext context) throws Exception {
        pipelineRunnerService.execute(context.getServiceContext());
        return context;
    }
}
