package gov.cdc.izgateway.xform.logging.advice;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
public class OperationAdviceDTO extends XformAdviceDTO {
    public OperationAdviceDTO() {
    }

    public OperationAdviceDTO(UUID id, String className, String name) {
        super(id, className, name);
    }
}
