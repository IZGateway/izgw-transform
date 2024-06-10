package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
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
