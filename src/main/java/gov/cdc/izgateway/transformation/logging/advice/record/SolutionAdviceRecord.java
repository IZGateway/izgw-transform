package gov.cdc.izgateway.transformation.logging.advice.record;

import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.logging.advice.MethodDisposition;
import lombok.Data;

@Data
public class SolutionAdviceRecord extends XformAdviceRecord {
    private final String id;
    private final boolean hasTransformed;

    public SolutionAdviceRecord(String id, boolean hasTransformed, String name, String className, String methodName, MethodDisposition methodDisposition, String requestMessage, String responseMessage, DataFlowDirection dataFlowDirection) {
        super(name, className, methodName, methodDisposition, requestMessage, responseMessage, dataFlowDirection);
        this.id = id;
        this.hasTransformed = hasTransformed;
    }

}
