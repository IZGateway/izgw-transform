package gov.cdc.izgateway.transformation.operations;

import gov.cdc.izgateway.transformation.logging.advice.Advisable;
import gov.cdc.izgateway.transformation.logging.advice.Transformable;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
abstract class BaseOperation implements Operation, Advisable, Transformable {
    private UUID id;
    private int order;

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
