package gov.cdc.izgateway.xform.logging.advice;

import lombok.Data;

import java.util.UUID;

/**
 * Advice recorded for a solution, solution operation or pipe by the advice aspect. Two instances are equal if they have the same identifier.
 */
@Data
public class SolutionAdviceDTO extends XformAdviceDTO {

    /**
     * Creates empty advice.
     */
    public SolutionAdviceDTO() {
    }

    /**
     * Creates advice for the identified advised object.
     *
     * @param id the identifier of the advised object
     * @param className the simple class name of the advised object
     * @param name the name of the advised object
     */
    public SolutionAdviceDTO(UUID id, String className, String name) {
        super(id, className, name);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SolutionAdviceDTO other) {
            return this.getId().equals(other.getId());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

}
