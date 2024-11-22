package gov.cdc.izgateway.xform.logging;

import lombok.Data;

@Data
public class XformSolutionLogDetail extends XformLogDetail {
    private String direction;
    private String pipelineName;
}
