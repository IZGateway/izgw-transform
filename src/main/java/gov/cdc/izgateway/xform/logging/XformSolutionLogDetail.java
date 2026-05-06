package gov.cdc.izgateway.xform.logging;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class XformSolutionLogDetail extends XformLogDetail {
    private String direction;
    private String pipelineName;
}
