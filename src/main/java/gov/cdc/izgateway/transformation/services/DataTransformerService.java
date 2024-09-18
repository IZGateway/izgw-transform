package gov.cdc.izgateway.transformation.services;

import gov.cdc.izgateway.transformation.context.HubWsdlTransformationContext;
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

    public HubWsdlTransformationContext transform(HubWsdlTransformationContext context) throws Exception {
        pipelineRunnerService.execute(context.getServiceContext());
        return context;
    }
}
