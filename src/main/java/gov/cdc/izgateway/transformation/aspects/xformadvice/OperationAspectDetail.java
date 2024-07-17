package gov.cdc.izgateway.transformation.aspects.xformadvice;

import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import lombok.Data;

@Data
public class OperationAspectDetail extends XformAspectDetail {
    private final boolean hasTransformed;

    public OperationAspectDetail(boolean hasTransformed, String name, String className, String methodName, MethodDisposition methodDisposition, String requestMessage, String responseMessage, DataFlowDirection dataFlowDirection) {
        super(name, className, methodName, methodDisposition, requestMessage, responseMessage, dataFlowDirection);
        this.hasTransformed = hasTransformed;
    }
}