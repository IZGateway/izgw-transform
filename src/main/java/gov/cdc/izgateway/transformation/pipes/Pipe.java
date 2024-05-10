package gov.cdc.izgateway.transformation.pipes;

import ca.uhn.hl7v2.model.Message;

public interface Pipe {
    void execute(Message message);
    void setNextPipe(Pipe pipe);
    Pipe getNextPipe();
}
