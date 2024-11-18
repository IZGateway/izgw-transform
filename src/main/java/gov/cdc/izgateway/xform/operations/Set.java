package gov.cdc.izgateway.xform.operations;

import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.enums.DataType;
import gov.cdc.izgateway.xform.exceptions.OperationException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Set extends BaseOperation implements Operation {

    private String destinationField;
    private String setValue;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Set.class);

    public Set() {}

    protected Set(Set set) {
        super(set.getId(), set.getOrder());
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
