package gov.cdc.izgateway.transformation.pipes;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;

public interface Pipe {
    void execute(Message message) throws HL7Exception;
    void setNextPipe(Pipe pipe);
    Pipe getNextPipe();
}
