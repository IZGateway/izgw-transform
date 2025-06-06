package gov.cdc.izgateway.xform.operations;

import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.enums.DataType;
import gov.cdc.izgateway.xform.exceptions.OperationException;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Mapper extends BaseOperation implements Operation {

    @NotBlank(message = "required and cannot be empty")
    private String codeField;
    @NotBlank(message = "required and cannot be empty")
    private String codeSystemField;
    @NotBlank(message = "required and cannot be empty")
    private String codeSystemDefault;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Mapper.class);

    protected Mapper() {}

    protected Mapper(Mapper mapper) {
        super(mapper.getId(), mapper.getOrder());
        this.codeField = mapper.getCodeField();
        this.codeSystemField = mapper.getCodeSystemField();
        this.codeSystemDefault = mapper.getCodeSystemDefault();
    }

    @Override
    public void execute(ServiceContext context) throws OperationException {
        if (log.isTraceEnabled()) {
            log.trace("MAP Operation: {} / CODE FIELD {} CODE SYSTEM FIELD {}", this.getClass().getSimpleName(), this.getCodeField(), this.getCodeSystemField());
        }

        if (context.getDataType().equals(DataType.HL7V2)) {
            Hl7v2Mapper copy = new Hl7v2Mapper(this);
            copy.execute(context);
        }
    }
}
