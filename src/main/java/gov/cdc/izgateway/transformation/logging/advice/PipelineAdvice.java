package gov.cdc.izgateway.transformation.logging.advice;

import gov.cdc.izgateway.transformation.logging.advice.record.SolutionAdviceRecord;
import gov.cdc.izgateway.transformation.logging.advice.record.XformAdviceRecord;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class PipelineAdvice extends XformAdvice {
    private Map<String, SolutionAdvice> solutionAdviceMap = new LinkedHashMap<>();
    private ArrayList<SolutionAdvice> solutionAdviceList = new ArrayList<>();
    private String id;

//    public PipelineAdvice() {
//        super("", "", "");
//    }

    public PipelineAdvice(String id, String className, String name) {
        super(className, name);
        this.id = id;
    }

    public SolutionAdvice getSolutionAdvice(SolutionAdviceRecord advice) {
        SolutionAdvice solutionAdvice = new SolutionAdvice(advice.getId(), advice.getClassName(), advice.getName());
        int adviceIndex = solutionAdviceList.indexOf(solutionAdvice);
        if ( adviceIndex >= 0 ) {
            return solutionAdviceList.get(adviceIndex);
        }

        solutionAdviceList.add(solutionAdvice);

        return solutionAdvice;
    }
}
