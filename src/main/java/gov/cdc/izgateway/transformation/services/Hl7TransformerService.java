package gov.cdc.izgateway.transformation.services;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.transformation.camel.HubWsdlTransformationContext;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.endpoints.hub.HubControllerFault;
import gov.cdc.izgateway.transformation.pipelines.Hl7Pipeline;
import gov.cdc.izgateway.transformation.pipelines.PipelineBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class Hl7TransformerService {
  PipelineBuilder pipelineBuilder;

  @Autowired
  public Hl7TransformerService(PipelineBuilder pipelineBuilder) {
    this.pipelineBuilder = pipelineBuilder;
  }

  // TODO PCAHILL ... left off where we were getting an HL7 PID not exist error - because the message we are sending has no PID
  public HubWsdlTransformationContext transform(HubWsdlTransformationContext context) throws Exception {
      try {
          String msg = context.getServiceContext().getRequestMessage().encode().replace("\r", "\n");
          log.info("Message pre-transformation:\n\n{}", msg);
      }
      catch (HL7Exception e) {
          throw new HubControllerFault(e.getMessage());
      }

      Hl7Pipeline pipeline = pipelineBuilder.build(context.getServiceContext());
      pipeline.execute(context.getServiceContext());

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
