package gov.cdc.izgateway.transformation.pipes;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.preconditions.Precondition;

public interface Pipe {
    void execute(ServiceContext context) throws HL7Exception;
    void executeCondition(ServiceContext context) throws HL7Exception;
    void addPrecondition(Precondition op);
    void setNextPipe(Pipe pipe);
    Pipe getNextPipe();
}
