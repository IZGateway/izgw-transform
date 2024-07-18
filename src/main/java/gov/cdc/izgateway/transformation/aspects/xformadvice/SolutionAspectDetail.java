package gov.cdc.izgateway.transformation.aspects.xformadvice;

import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import lombok.Data;

@Data
public class SolutionAspectDetail extends XformAspectDetail {
    private final String id;
    private final boolean hasTransformed;

    public SolutionAspectDetail(String id, boolean hasTransformed, String name, String className, String methodName, MethodDisposition methodDisposition, String requestMessage, String responseMessage, DataFlowDirection dataFlowDirection) {
        //super(name, className, methodName, methodDisposition, requestMessage, responseMessage, dataFlowDirection);
        this.id = id;
        this.hasTransformed = hasTransformed;
    }

}
