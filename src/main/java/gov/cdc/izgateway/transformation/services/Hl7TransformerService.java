package gov.cdc.izgateway.transformation.services;

import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.pipelines.Hl7Pipeline;
import gov.cdc.izgateway.transformation.pipelines.PipelineBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Hl7TransformerService {
  PipelineBuilder pipelineBuilder;

  @Autowired
  public Hl7TransformerService(PipelineBuilder pipelineBuilder) {
    this.pipelineBuilder = pipelineBuilder;
  }

  // TODO PCAHILL ... left off where we were getting an HL7 PID not exist error - because the message we are sending has no PID
  public ServiceContext transform(ServiceContext context) throws Exception {
      Hl7Pipeline pipeline = pipelineBuilder.build(context);
      pipeline.execute(context);

      return context;
  }

}
