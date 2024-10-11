package gov.cdc.izgateway.transformation.operations;

import gov.cdc.izgateway.transformation.annotations.CaptureXformAdvice;
import gov.cdc.izgateway.transformation.annotations.ExcludeField;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataType;
import gov.cdc.izgateway.transformation.exceptions.OperationException;
import gov.cdc.izgateway.transformation.logging.advice.Advisable;
import gov.cdc.izgateway.transformation.logging.advice.Transformable;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SaveState implements Operation, Advisable, Transformable {

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
    @CaptureXformAdvice
    public void execute(ServiceContext context) throws OperationException {
        if (log.isTraceEnabled()) {
            log.trace("Operation: {} / Save value from {} To key {}", this.getClass().getSimpleName(), this.field, this.key);
        }

        if (context.getDataType().equals(DataType.HL7V2)) {
            Hl7v2SaveState saveState = new Hl7v2SaveState(this);
            saveState.execute(context);
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean hasTransformed() {
        return false;
    }
}
