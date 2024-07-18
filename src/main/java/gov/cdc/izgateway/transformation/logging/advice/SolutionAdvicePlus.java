package gov.cdc.izgateway.transformation.logging.advice;

import lombok.Data;

import java.util.ArrayList;

@Data
public class SolutionAdvicePlus extends SolutionAdvice {
    private final ArrayList<OperationAdvice> requestOperationAdviceList = new ArrayList<>();
    private final ArrayList<OperationAdvice> responseOperationAdviceList = new ArrayList<>();

    public SolutionAdvicePlus(String id, String className, String name) {
        super(id, className, name);
    }

    public void addRequestOperationAdvice(OperationAdvice operationAdvice) {
        requestOperationAdviceList.add(operationAdvice);
    }

    public void addResponseOperationAdvice(OperationAdvice operationAdvice) {
        responseOperationAdviceList.add(operationAdvice);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SolutionAdvicePlus other) {
            return this.getId().equals(other.getId());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

}
