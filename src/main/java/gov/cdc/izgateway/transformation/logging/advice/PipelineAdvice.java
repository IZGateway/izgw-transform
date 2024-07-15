package gov.cdc.izgateway.transformation.logging.advice;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class PipelineAdvice extends XformAdvice {
    private Map<String, SolutionAdvice> solutionAdviceMap = new LinkedHashMap<>();

    public PipelineAdvice() {
        super("", "");
    }

    public PipelineAdvice(String className, String name) {
        super(className, name);
    }

    public SolutionAdvice getSolutionAdvice(XformAdviceRecord advice) {
        SolutionAdvice solutionAdvice = solutionAdviceMap.containsKey(advice.descriptorId()) ?
                solutionAdviceMap.get(advice.descriptorId()) : new SolutionAdvice(advice.className(), advice.descriptor());

        solutionAdviceMap.put(advice.descriptorId(), solutionAdvice);

        return solutionAdvice;
    }
}
