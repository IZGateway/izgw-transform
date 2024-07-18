package gov.cdc.izgateway.transformation.logging.advice;

import lombok.Data;

@Data
public class OperationAdviceDTO extends XformAdviceDTO {
    public OperationAdviceDTO() {
    }

    public OperationAdviceDTO(String className, String name) {
        super(className, name);
    }
}
