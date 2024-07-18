package gov.cdc.izgateway.transformation.aspects.xformadvice;

import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.logging.advice.XformAdvice;
import lombok.Data;

@Data
public class XformAspectDetail extends XformAdvice {
    private DataFlowDirection dataFlowDirection;

    public XformAspectDetail() {
    }

}
