package gov.cdc.izgateway.transformation.logging.advice;

import lombok.Data;

import java.util.ArrayList;

@Data
public class PipelineAdvice extends PipelineAdviceDTO {
    private ArrayList<SolutionAdvice> solutionAdviceList = new ArrayList<>();
    private String id;

    public PipelineAdvice(PipelineAdviceDTO advice) {
        this.setClassName(advice.getClassName());
        this.setName(advice.getName());
        this.setRequest(advice.getRequest());
        this.setTransformedRequest(advice.getTransformedRequest());
        this.setResponse(advice.getResponse());
        this.setTransformedResponse(advice.getTransformedResponse());
        this.setId(advice.getId());
    }

    public SolutionAdvice getSolutionAdvice(SolutionAdviceDTO advice) {
        SolutionAdvice solutionAdvice = new SolutionAdvice(advice.getId(), advice.getClassName(), advice.getName());
        int adviceIndex = solutionAdviceList.indexOf(solutionAdvice);
        if ( adviceIndex >= 0 ) {
            return solutionAdviceList.get(adviceIndex);
        }

        solutionAdviceList.add(solutionAdvice);

        return solutionAdvice;
    }
}
