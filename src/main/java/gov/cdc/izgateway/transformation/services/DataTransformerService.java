package gov.cdc.izgateway.transformation.services;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.transformation.context.HubWsdlTransformationContext;
import gov.cdc.izgateway.transformation.endpoints.hub.HubControllerFault;
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
        try {
            String msg = context.getServiceContext().getRequestMessage().encode().replace("\r", "\n");
            log.trace("Message pre-transformation:\n\n{}", msg);
        }
        catch (HL7Exception e) {
            throw new HubControllerFault(e.getMessage());
        }

        pipelineRunnerService.execute(context.getServiceContext());

        try {
            String msg = context.getServiceContext().getRequestMessage().encode().replace("\r", "\n");
            log.trace("Message post-transformation:\n\n{}", msg);
        }
        catch (HL7Exception e) {
            throw new HubControllerFault(e.getMessage());
        }

        return context;
    }
}
