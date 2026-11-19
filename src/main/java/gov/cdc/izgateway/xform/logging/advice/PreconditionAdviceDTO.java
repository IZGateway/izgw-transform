package gov.cdc.izgateway.xform.logging.advice;

import lombok.Data;

import java.util.UUID;

/**
 * Advice recorded for a precondition by the advice aspect. Two instances are equal if they have the same identifier.
 */
@Data
public class PreconditionAdviceDTO extends XformAdviceDTO {
    /**
     * Creates empty advice.
     */
    public PreconditionAdviceDTO() {
    }

    /**
     * Creates advice for the identified advised object.
     *
     * @param id the identifier of the advised object
     * @param className the simple class name of the advised object
     * @param name the name of the advised object
     */
    public PreconditionAdviceDTO(UUID id, String className, String name) {
        super(id, className, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PreconditionAdviceDTO other) {
            return this.getId().equals(other.getId());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

}
