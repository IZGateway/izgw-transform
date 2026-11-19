package gov.cdc.izgateway.xform.logging.advice;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * Advice recorded for an operation by the advice aspect.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OperationAdviceDTO extends XformAdviceDTO {
    /**
     * Creates empty advice.
     */
    public OperationAdviceDTO() {
    }

    /**
     * Creates advice for the identified advised object.
     *
     * @param id the identifier of the advised object
     * @param className the simple class name of the advised object
     * @param name the name of the advised object
     */
    public OperationAdviceDTO(UUID id, String className, String name) {
        super(id, className, name);
    }
}
