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
public class Copy implements Operation, Advisable, Transformable {

    private UUID id;
    private int order;
    private String sourceField;
    private String destinationField;

    @ExcludeField
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Copy.class);

    public Copy() {}

    protected Copy(Copy copy) {
        this.id = copy.id;
        this.order = copy.order;
        this.sourceField = copy.sourceField;
        this.destinationField = copy.destinationField;
    }

    @Override
    @CaptureXformAdvice
    public void execute(ServiceContext context) throws OperationException {
        if (log.isTraceEnabled()) {
            log.trace("Copy Operation: {} / Copy {} TO {}", this.getClass().getSimpleName(), this.sourceField, this.destinationField);
        }

        if (context.getDataType().equals(DataType.HL7V2)) {
            Hl7v2Copy copy = new Hl7v2Copy(this);
            copy.execute(context);
        }

    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getId() {
        return this.id.toString();
    }

    @Override
    public boolean hasTransformed() {
        return true;
    }
}
