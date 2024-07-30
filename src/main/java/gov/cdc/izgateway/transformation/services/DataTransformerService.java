package gov.cdc.izgateway.transformation.services;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.transformation.context.HubWsdlTransformationContext;
import gov.cdc.izgateway.transformation.endpoints.hub.HubControllerFault;
import gov.cdc.izgateway.transformation.pipelines.DataPipeline;
import gov.cdc.izgateway.transformation.pipelines.Hl7Pipeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DataTransformerService {
    private final DataPipeline dataPipeline;

    @Autowired
    public DataTransformerService(DataPipeline dataPipeline) {
        this.dataPipeline = dataPipeline;
    }

    public HubWsdlTransformationContext transform(HubWsdlTransformationContext context) throws Exception {
        try {
            String msg = context.getServiceContext().getRequestMessage().encode().replace("\r", "\n");
            log.info("Message pre-transformation:\n\n{}", msg);
        }
        catch (HL7Exception e) {
            throw new HubControllerFault(e.getMessage());
        }

        dataPipeline.execute(context.getServiceContext());

        try {
            String msg = context.getServiceContext().getRequestMessage().encode().replace("\r", "\n");
            log.info("Message post-transformation:\n\n{}", msg);
        }
        catch (HL7Exception e) {
            throw new HubControllerFault(e.getMessage());
        }

        return context;
    }
}
