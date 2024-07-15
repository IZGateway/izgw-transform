package gov.cdc.izgateway.transformation.logging.advice;

import lombok.Data;

@Data
public class OperationAdvice extends XformAdvice {
    public OperationAdvice(String className, String name) {
        super(className, name);
    }
}
