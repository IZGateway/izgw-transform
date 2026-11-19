package gov.cdc.izgateway.xform.logging.advice;

import lombok.Data;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Collected advice for one solution, with the advice of its request and response operations and preconditions.
 */
@Data
public class SolutionAdvice extends SolutionAdviceDTO {
    private final ArrayList<PreconditionAdviceDTO> requestPreconditionAdviceList = new ArrayList<>();
    private final ArrayList<PreconditionAdviceDTO> responsePreconditionAdviceList = new ArrayList<>();
    private final ArrayList<OperationAdviceDTO> requestOperationAdviceList = new ArrayList<>();
    private final ArrayList<OperationAdviceDTO> responseOperationAdviceList = new ArrayList<>();

    /**
     * Creates collected advice for the identified solution.
     *
     * @param id the identifier of the solution
     * @param className the simple class name of the advised object
     * @param name the name of the solution
     */
    public SolutionAdvice(UUID id, String className, String name) {
        super(id, className, name);
    }

    /**
     * Adds advice for an operation that transformed the request.
     *
     * @param operationAdvice the operation advice to add
     */
    public void addRequestOperationAdvice(OperationAdviceDTO operationAdvice) {
        requestOperationAdviceList.add(operationAdvice);
    }

    /**
     * Adds advice for an operation that transformed the response.
     *
     * @param operationAdvice the operation advice to add
     */
    public void addResponseOperationAdvice(OperationAdviceDTO operationAdvice) {
        responseOperationAdviceList.add(operationAdvice);
    }

    /**
     * Adds request precondition advice if it is not in the list.
     *
     * @param advice the precondition advice to add
     */
    public void addRequestPreconditionAdvice(PreconditionAdviceDTO advice) {
        int adviceIndex = requestPreconditionAdviceList.indexOf(advice);
        if ( adviceIndex < 0 ) {
            requestPreconditionAdviceList.add(advice);
        }
    }

    /**
     * Adds response precondition advice if it is not in the list.
     *
     * @param advice the precondition advice to add
     */
    public void addResponsePreconditionAdvice(PreconditionAdviceDTO advice) {
        int adviceIndex = responsePreconditionAdviceList.indexOf(advice);
        if ( adviceIndex < 0 ) {
            responsePreconditionAdviceList.add(advice);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SolutionAdviceDTO other) {
            return this.getId().equals(other.getId());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

}
