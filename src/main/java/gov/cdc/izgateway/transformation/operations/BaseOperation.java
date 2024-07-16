package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.transformation.annotations.CaptureXformAdvice;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.logging.advice.Advisable;

abstract class BaseOperation<T> implements Operation, Advisable {

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
    @CaptureXformAdvice
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

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getId() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean hasTransformed() {
        return true;
    }

    @Override
    public boolean preconditionPassed() {
        return true;
    }
}
