package gov.cdc.izgateway.transformation.operations;

import gov.cdc.izgateway.transformation.annotations.ExcludeField;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.exceptions.OperationException;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SaveState implements Operation {

    private UUID id;
    private int order;
    private String field;
    private String key;

    @ExcludeField
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SaveState.class);

    public SaveState() {}

    protected SaveState(SaveState saveState) {
        this.id = saveState.id;
        this.order = saveState.order;
        this.field = saveState.field;
        this.key = saveState.key;
    }

    @Override
    public void execute(ServiceContext context) throws OperationException {
        if (log.isTraceEnabled()) {
            log.trace("Operation: {} / Save value from {} To key {}", this.getClass().getSimpleName(), this.field, this.key);
        }
    }
}
