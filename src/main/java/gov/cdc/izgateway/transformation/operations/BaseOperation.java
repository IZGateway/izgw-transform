package gov.cdc.izgateway.transformation.operations;

import gov.cdc.izgateway.transformation.annotations.CaptureXformAdvice;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.exceptions.OperationException;
import gov.cdc.izgateway.transformation.logging.advice.Advisable;
import gov.cdc.izgateway.transformation.logging.advice.Transformable;

abstract class BaseOperation<T> implements Operation, Advisable, Transformable {

    Operation nextOperation;
    T operationConfig;

    protected BaseOperation(T config) {
        this.operationConfig = config;
    }

    @Override
    @CaptureXformAdvice
    public final void execute(ServiceContext context) throws OperationException {
            thisOperation(context);
            nextOperation(context);
    }

    public abstract void thisOperation(ServiceContext context) throws OperationException;

    public void nextOperation(ServiceContext context) throws OperationException {
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
}
