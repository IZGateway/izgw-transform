package gov.cdc.izgateway.xform.logging.advice;

import lombok.Data;

import java.util.UUID;

@Data
public class PreconditionAdviceDTO extends XformAdviceDTO {
    public PreconditionAdviceDTO() {
    }

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
