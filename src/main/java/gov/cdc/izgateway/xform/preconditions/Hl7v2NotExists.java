package gov.cdc.izgateway.xform.preconditions;

import gov.cdc.izgateway.xform.context.ServiceContext;

public class Hl7v2NotExists extends NotExists implements Precondition {

    public Hl7v2NotExists() {
        super();
    }

    public Hl7v2NotExists(NotExists notExists) {
        super(notExists);
    }

    @Override
    public boolean evaluate(ServiceContext context) {
        Hl7v2Exists exists = new Hl7v2Exists(this);
        return !exists.evaluate(context);
    }
}
