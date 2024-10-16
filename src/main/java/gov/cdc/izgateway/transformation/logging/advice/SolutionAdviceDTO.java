package gov.cdc.izgateway.transformation.logging.advice;

import lombok.Data;

import java.util.UUID;

@Data
public class SolutionAdviceDTO extends XformAdviceDTO {

    public SolutionAdviceDTO() {
    }

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
