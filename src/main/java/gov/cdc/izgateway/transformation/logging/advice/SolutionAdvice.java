package gov.cdc.izgateway.transformation.logging.advice;

import lombok.Data;

import java.util.ArrayList;

@Data
public class SolutionAdvice extends XformAdvice {
    private final ArrayList<OperationAdvice> requestOperationAdviceList = new ArrayList<>();
    private final ArrayList<OperationAdvice> responseOperationAdviceList = new ArrayList<>();

    public SolutionAdvice(String className, String name) {
        super(className, name);
    }

    public void addRequestOperationAdvice(OperationAdvice operationAdvice) {
        requestOperationAdviceList.add(operationAdvice);
    }

    public void addResponseOperationAdvice(OperationAdvice operationAdvice) {
        responseOperationAdviceList.add(operationAdvice);
    }
}
