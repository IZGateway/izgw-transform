package gov.cdc.izgateway.xform.operations;

import gov.cdc.izgateway.xform.logging.advice.Advisable;
import gov.cdc.izgateway.xform.logging.advice.Transformable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
abstract class BaseOperation implements Operation, Advisable, Transformable {
    @NotNull(message = "required and cannot be empty")
    private UUID id;
    @NotNull(message = "required")
    private Integer order;

    protected BaseOperation() {}

    protected BaseOperation(UUID id, int order) {
        this.id = id;
        this.order = order;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean hasTransformed() {
        return true;
    }
}
