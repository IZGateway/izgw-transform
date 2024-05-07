package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;

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

}
