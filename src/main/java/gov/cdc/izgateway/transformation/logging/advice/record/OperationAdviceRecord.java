package gov.cdc.izgateway.transformation.logging.advice.record;

import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.logging.advice.MethodDisposition;
import lombok.Data;

@Data
public class OperationAdviceRecord extends XformAdviceRecord {
    private final boolean hasTransformed;

    public OperationAdviceRecord(boolean hasTransformed, String name, String className, String methodName, MethodDisposition methodDisposition, String requestMessage, String responseMessage, DataFlowDirection dataFlowDirection) {
        super(name, className, methodName, methodDisposition, requestMessage, responseMessage, dataFlowDirection);
        this.hasTransformed = hasTransformed;
    }
}