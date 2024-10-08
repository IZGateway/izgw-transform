package gov.cdc.izgateway.transformation.operations;

import gov.cdc.izgateway.transformation.annotations.ExcludeField;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataType;
import gov.cdc.izgateway.transformation.exceptions.OperationException;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Set implements Operation {

    private UUID id;
    private int order;
    private String destinationField;
    private String setValue;

    @ExcludeField
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Set.class);

    public Set() {}

    protected Set(Set set) {
        this.id = set.id;
        this.order = set.order;
        this.destinationField = set.destinationField;
        this.setValue = set.setValue;
    }


    @Override
    public void execute(ServiceContext context) throws OperationException {
        if (log.isTraceEnabled()) {
            log.trace("SET Operation: {} / SET {} TO {}", this.getClass().getSimpleName(), this.destinationField, this.setValue);
        }

        if (context.getDataType().equals(DataType.HL7V2)) {
            Hl7v2Set hl7Set = new Hl7v2Set(this);
            hl7Set.execute(context);
        }
    }
}
