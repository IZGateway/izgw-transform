package gov.cdc.izgateway.transformation.logging.advice;

import lombok.Data;

import java.util.ArrayList;
import java.util.UUID;

@Data
public class SolutionAdvice extends SolutionAdviceDTO {
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SolutionAdvice other) {
            return this.getId().equals(other.getId());
        }

        return false;
    }

}
