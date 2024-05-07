package gov.cdc.izgateway.transformation.operations;

abstract class BaseConditionalOperation<T> implements ConditionalOperation {

    T operationConfig;

    protected BaseConditionalOperation(T configuration) {
        this.operationConfig = configuration;
    }

}
