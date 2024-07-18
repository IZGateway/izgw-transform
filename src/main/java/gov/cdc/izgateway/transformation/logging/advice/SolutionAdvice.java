package gov.cdc.izgateway.transformation.logging.advice;

import lombok.Data;

import java.util.ArrayList;

@Data
public class SolutionAdvice extends XformAdvice {
    private String id;

    public SolutionAdvice() {
    }

    public SolutionAdvice(String id, String className, String name) {
        super(className, name);
        this.id = id;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SolutionAdvice other) {
            return this.getId().equals(other.getId());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

}
