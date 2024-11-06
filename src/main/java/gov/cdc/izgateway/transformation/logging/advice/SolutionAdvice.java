package gov.cdc.izgateway.transformation.logging.advice;

import lombok.Data;

import java.util.ArrayList;
import java.util.UUID;

@Data
public class SolutionAdvice extends SolutionAdviceDTO {
    private final ArrayList<PreconditionAdviceDTO> requestPreconditionAdviceList = new ArrayList<>();
    private final ArrayList<PreconditionAdviceDTO> responsePreconditionAdviceList = new ArrayList<>();
    private final ArrayList<OperationAdviceDTO> requestOperationAdviceList = new ArrayList<>();
    private final ArrayList<OperationAdviceDTO> responseOperationAdviceList = new ArrayList<>();

    public SolutionAdvice(UUID id, String className, String name) {
        super(id, className, name);
    }

    public void addRequestOperationAdvice(OperationAdviceDTO operationAdvice) {
        requestOperationAdviceList.add(operationAdvice);
    }

    public void addResponseOperationAdvice(OperationAdviceDTO operationAdvice) {
        responseOperationAdviceList.add(operationAdvice);
    }

    public void addRequestPreconditionAdvice(PreconditionAdviceDTO advice) {
        PreconditionAdvice preconditionAdvice = new PreconditionAdvice(advice.getId(), advice.getClassName(), advice.getName());
        int adviceIndex = requestPreconditionAdviceList.indexOf(preconditionAdvice);
        if ( adviceIndex < 0 ) {
            requestPreconditionAdviceList.add(preconditionAdvice);
        }
    }

    public void addResponsePreconditionAdvice(PreconditionAdviceDTO advice) {
        PreconditionAdvice preconditionAdvice = new PreconditionAdvice(advice.getId(), advice.getClassName(), advice.getName());
        int adviceIndex = responsePreconditionAdviceList.indexOf(preconditionAdvice);
        if ( adviceIndex < 0 ) {
            responsePreconditionAdviceList.add(preconditionAdvice);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SolutionAdvice other) {
            return this.getId().equals(other.getId());
        }

        return false;
    }

}
