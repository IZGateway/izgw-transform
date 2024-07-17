package gov.cdc.izgateway.transformation.logging.advice.record;

import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.logging.advice.MethodDisposition;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class PipelineAdviceRecord extends XformAdviceRecord {
    private final String id;

    public PipelineAdviceRecord(String id, String name, String className, String methodName, MethodDisposition methodDisposition, String requestMessage, String responseMessage, DataFlowDirection dataFlowDirection) {
        super(name, className, methodName, methodDisposition, requestMessage, responseMessage, dataFlowDirection);
        this.id = id;
    }
}
