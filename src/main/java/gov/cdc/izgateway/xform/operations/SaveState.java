package gov.cdc.izgateway.xform.operations;

import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.enums.DataType;
import gov.cdc.izgateway.xform.exceptions.OperationException;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveState extends BaseOperation implements Operation {

    @NotBlank(message = "required and cannot be empty")
    private String field;
    @NotBlank(message = "required and cannot be empty")
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
