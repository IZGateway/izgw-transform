package gov.cdc.izgateway.transformation.operations;

import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataType;
import gov.cdc.izgateway.transformation.exceptions.OperationException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Mapper extends BaseOperation implements Operation {

    private String codeField;
    private String codeSystemField;
    private String codeSystemDefault;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Mapper.class);

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
