package gov.cdc.izgateway.xform.logging.advice;

import lombok.Data;

import java.util.UUID;

@Data
public class OperationAdviceDTO extends XformAdviceDTO {
    public OperationAdviceDTO() {
    }

    public OperationAdviceDTO(UUID id, String className, String name) {
        super(id, className, name);
    }
}
