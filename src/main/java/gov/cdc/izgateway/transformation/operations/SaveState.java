package gov.cdc.izgateway.transformation.operations;

import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataType;
import gov.cdc.izgateway.transformation.exceptions.OperationException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveState extends BaseOperation implements Operation {

    private String field;
    private String key;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SaveState.class);

    public SaveState() {}

    protected SaveState(SaveState saveState) {
        super(saveState.getId(), saveState.getOrder());
        this.field = saveState.field;
        this.key = saveState.key;
    }

    @Override
    public void execute(ServiceContext context) throws OperationException {
        if (log.isTraceEnabled()) {
            log.trace("Operation: {} / Save value from {} To key {}", this.getClass().getSimpleName(), this.field, this.key);
        }

        if (context.getDataType().equals(DataType.HL7V2)) {
            Hl7v2SaveState saveState = new Hl7v2SaveState(this);
            saveState.execute(context);
        }
    }
}
