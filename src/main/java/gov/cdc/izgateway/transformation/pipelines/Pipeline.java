package gov.cdc.izgateway.transformation.pipelines;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;

public interface Pipeline {

    void execute(Message message, String direction) throws HL7Exception;

    void setNextPipeline(Pipeline nextPipeline);

    Pipeline getNextPipeline();

}
