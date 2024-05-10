package gov.cdc.izgateway.transformation.pipelines;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.pipes.Pipe;

public interface Pipeline {

    // TODO - if throw stays needs to be generic, not everything will be HL7
    void execute(Message message, String direction) throws HL7Exception;

    // TODO - remove once refactor finishes "Next" will exist on Pipes in Pipeline
    void setNextPipeline(Pipeline nextPipeline);

    // TODO - remove once refactor finishes "Next" will exist on Pipes in Pipeline
    Pipeline getNextPipeline();

}
