package gov.cdc.izgateway.transformation.pipes;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.context.ServiceContext;

public interface Pipe {
    void execute(ServiceContext context) throws HL7Exception;
    void setNextPipe(Pipe pipe);
    Pipe getNextPipe();
}
