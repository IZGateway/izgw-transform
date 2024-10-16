package gov.cdc.izgateway.transformation.operations;

import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataType;
import gov.cdc.izgateway.transformation.exceptions.OperationException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Copy extends BaseOperation implements Operation {

    private String sourceField;
    private String destinationField;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Copy.class);

    public Copy() {
    }

    protected Copy(Copy copy) {
        super(copy.getId(), copy.getOrder());
        this.sourceField = copy.sourceField;
        this.destinationField = copy.destinationField;
    }

    @Override
    public void execute(ServiceContext context) throws OperationException {
        if (log.isTraceEnabled()) {
            log.trace("Copy Operation: {} / Copy {} TO {}", this.getClass().getSimpleName(), this.sourceField, this.destinationField);
        }

        if (context.getDataType().equals(DataType.HL7V2)) {
            Hl7v2Copy copy = new Hl7v2Copy(this);
            copy.execute(context);
        }

    }

}
