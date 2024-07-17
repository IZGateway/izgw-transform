package gov.cdc.izgateway.transformation.aspects.xformadvice;

import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import lombok.Data;

@Data
public class PipelineAspectDetail extends XformAspectDetail {
    private final String id;

    public PipelineAspectDetail(String id, String name, String className, String methodName, MethodDisposition methodDisposition, String requestMessage, String responseMessage, DataFlowDirection dataFlowDirection) {
        super(name, className, methodName, methodDisposition, requestMessage, responseMessage, dataFlowDirection);
        this.id = id;
    }
}
