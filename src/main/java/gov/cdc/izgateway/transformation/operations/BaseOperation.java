package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.context.ServiceContext;

abstract class BaseOperation<T> implements Operation {

    Operation nextOperation;
    T operationConfig;

    protected BaseOperation(T config) {
        this.operationConfig = config;
    }

    @Override
    public void setNextOperation(Operation nextOperation) {
        this.nextOperation = nextOperation;
    }

    @Override
    public Operation getNextOperation() {
        return nextOperation;
    }

    @Override
    public final void transform(Message message) throws HL7Exception {
        executeOperation(message);
        executeNextOperation(message);
    }

    public abstract void executeOperation(Message message) throws HL7Exception;

    public void executeNextOperation(Message message) throws HL7Exception {
        if (nextOperation != null) {
            nextOperation.transform(message);
        }
    }

    @Override
    public final void execute(ServiceContext context) throws HL7Exception {
        thisOperation(context);
        nextOperation(context);
    }

    public abstract void thisOperation(ServiceContext context) throws HL7Exception;

    public void nextOperation(ServiceContext context) throws HL7Exception {
        if (nextOperation != null) {
            nextOperation.execute(context);
        }
    }

}
