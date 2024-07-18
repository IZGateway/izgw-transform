package gov.cdc.izgateway.transformation.logging.advice;

import gov.cdc.izgateway.transformation.aspects.xformadvice.SolutionAspectDetail;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class PipelineAdvice extends XformAdvice {
    private String id;


    public PipelineAdvice() {
    }

    public PipelineAdvice(String id, String className, String name) {
        super(className, name);
        this.id = id;
    }

}
