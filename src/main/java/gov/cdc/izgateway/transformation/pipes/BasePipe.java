package gov.cdc.izgateway.transformation.pipes;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.configuration.PipeConfig;

abstract class BasePipe implements Pipe {
    private Pipe next;
    private PipeConfig configuration;

    protected BasePipe(PipeConfig configuration) {
        this.configuration = configuration;
    }


    @Override
    public void execute(Message message) throws HL7Exception {
        executeThisPipe(message);
        executeNextPipe(message);
    }

    @Override
    public void setNextPipe(Pipe pipe) {
        this.next = pipe;
    }

    @Override
    public Pipe getNextPipe() {
        return next;
    }

    public abstract void executeThisPipe(Message message) throws HL7Exception;

    private void executeNextPipe(Message message) throws HL7Exception {
        if (next != null) {
            next.execute(message);
        }
    }
}
