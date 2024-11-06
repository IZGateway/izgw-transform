package gov.cdc.izgateway.transformation.logging.advice;

import lombok.Data;

import java.util.UUID;

@Data
public class PreconditionAdvice extends PreconditionAdviceDTO {

    public PreconditionAdvice(UUID id, String className, String name) {
        super(id, className, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PreconditionAdvice other) {
            return this.getId().equals(other.getId());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
