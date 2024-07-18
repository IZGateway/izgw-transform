package gov.cdc.izgateway.transformation.logging.advice;

import gov.cdc.izgateway.transformation.aspects.xformadvice.SolutionAspectDetail;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class PipelineAdvicePlus {
    private PipelineAdvice pipelineAdvice;
    private Map<String, SolutionAdvicePlus> solutionAdviceMap = new LinkedHashMap<>();
    private ArrayList<SolutionAdvicePlus> solutionAdviceList = new ArrayList<>();
    private String id;

    public PipelineAdvicePlus() {
    }


    public SolutionAdvicePlus getSolutionAdvice(SolutionAdvice advice) {
        SolutionAdvicePlus solutionAdvice = new SolutionAdvicePlus(advice.getId(), advice.getClassName(), advice.getName());
        int adviceIndex = solutionAdviceList.indexOf(solutionAdvice);
        if ( adviceIndex >= 0 ) {
            return solutionAdviceList.get(adviceIndex);
        }

        solutionAdviceList.add(solutionAdvice);

        return solutionAdvice;
    }
}
