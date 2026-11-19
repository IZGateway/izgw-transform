package gov.cdc.izgateway.xform.preconditions;

import gov.cdc.izgateway.xform.context.ServiceContext;

/**
 * HL7 v2 implementation of the {@link NotExists} precondition.
 */
public class Hl7v2NotExists extends NotExists implements Precondition {

    /**
     * Creates an empty precondition.
     */
    public Hl7v2NotExists() {
        super();
    }

    /**
     * Creates an HL7 v2 precondition with the settings of another precondition.
     * @param notExists the precondition to copy the settings from
     */
    public Hl7v2NotExists(NotExists notExists) {
        super(notExists);
    }

    @Override
    public boolean evaluate(ServiceContext context) {
        Hl7v2Exists exists = new Hl7v2Exists(this);
        return !exists.evaluate(context);
    }
}
