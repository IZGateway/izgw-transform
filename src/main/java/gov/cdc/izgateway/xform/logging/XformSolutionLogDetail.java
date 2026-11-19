package gov.cdc.izgateway.xform.logging;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Log detail for one solution of a pipeline, with the message direction it transformed.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class XformSolutionLogDetail extends XformLogDetail {
    private String direction;
    private String pipelineName;

    /**
     * Creates an empty solution log detail.
     */
    public XformSolutionLogDetail() {
        // Lombok generates the accessors.
    }
}
