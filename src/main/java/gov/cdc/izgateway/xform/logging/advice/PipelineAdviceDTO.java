package gov.cdc.izgateway.xform.logging.advice;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * Advice recorded for a pipeline run by the advice aspect.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PipelineAdviceDTO extends XformAdviceDTO {
    /**
     * Creates empty advice.
     */
    public PipelineAdviceDTO() {
    }

    /**
     * Creates advice for the identified advised object.
     *
     * @param id the identifier of the advised object
     * @param className the simple class name of the advised object
     * @param name the name of the advised object
     */
    public PipelineAdviceDTO(UUID id, String className, String name) {
        super(id, className, name);
    }
}
