package gov.cdc.izgateway.transformation.logging.advice.record;

import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.logging.advice.MethodDisposition;
import lombok.Data;

/**
 * This record represents a transformation advice.  Transformation advice is used to log the state of a transformation
 * as it is being processed.  This data will be used to create header records in the response messages.  The advice
 * shows the consumer the state of the transformation at the time of the advice.
 */
@Data
public class XformAdviceRecord {
    private String name;
    private String className;
    private String methodName;
    private MethodDisposition methodDisposition;
    private String requestMessage;
    private String responseMessage;
    private DataFlowDirection dataFlowDirection;

    public XformAdviceRecord() {
    }

    public XformAdviceRecord(String name, String className, String methodName, MethodDisposition methodDisposition, String requestMessage, String responseMessage, DataFlowDirection dataFlowDirection) {
        this.name = name;
        this.className = className;
        this.methodName = methodName;
        this.methodDisposition = methodDisposition;
        this.requestMessage = requestMessage;
        this.responseMessage = responseMessage;
        this.dataFlowDirection = dataFlowDirection;
    }
}
