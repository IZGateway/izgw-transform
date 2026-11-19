package gov.cdc.izgateway.xform.logging.advice;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Collected advice for one pipeline run, with the advice of its solutions and pipeline-level preconditions.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PipelineAdvice extends PipelineAdviceDTO {
    private ArrayList<SolutionAdvice> solutionAdviceList = new ArrayList<>();
    private ArrayList<PreconditionAdviceDTO> preconditionAdviceList = new ArrayList<>();
    private UUID id;
    private boolean requestTransformed = false;
    private boolean responseTransformed = false;

    /**
     * Creates the collected pipeline advice from the first advice recorded for the pipeline.
     *
     * @param advice the pipeline advice to copy
     */
    public PipelineAdvice(PipelineAdviceDTO advice) {
        this.setClassName(advice.getClassName());
        this.setName(advice.getName());
        this.setRequest(advice.getRequest());
        this.setTransformedRequest(advice.getTransformedRequest());
        this.setResponse(advice.getResponse());
        this.setTransformedResponse(advice.getTransformedResponse());
        this.setId(advice.getId());
    }

    /**
     * Returns the collected advice for the solution with the identifier of the given advice, and adds it if it is not in the list.
     *
     * @param advice the solution advice to find
     * @return the collected solution advice
     */
    public SolutionAdvice getSolutionAdvice(SolutionAdviceDTO advice) {
        SolutionAdvice solutionAdvice = new SolutionAdvice(advice.getId(), advice.getClassName(), advice.getName());
        int adviceIndex = solutionAdviceList.indexOf(solutionAdvice);
        if ( adviceIndex >= 0 ) {
            return solutionAdviceList.get(adviceIndex);
        }

        solutionAdviceList.add(solutionAdvice);

        return solutionAdvice;
    }

    /**
     * Adds the pipeline-level precondition advice if it is not in the list.
     *
     * @param advice the precondition advice to add
     */
    public void addPreconditionAdvice(PreconditionAdviceDTO advice) {
        int adviceIndex = preconditionAdviceList.indexOf(advice);
        if ( adviceIndex < 0 ) {
            preconditionAdviceList.add(advice);
        }
    }

    /**
     * Returns true if the precondition advice is in the pipeline-level list.
     *
     * @param precondition the precondition advice to find
     * @return true if the list contains advice with the same identifier
     */
    public boolean preconditionExists(PreconditionAdviceDTO precondition) {
        return (preconditionAdviceList.contains(precondition));
    }
}
