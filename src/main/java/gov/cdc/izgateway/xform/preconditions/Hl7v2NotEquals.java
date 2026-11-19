package gov.cdc.izgateway.xform.preconditions;

import gov.cdc.izgateway.xform.context.ServiceContext;

/**
 * HL7 v2 implementation of the {@link NotEquals} precondition.
 */
public class Hl7v2NotEquals extends NotEquals implements Precondition {
    /**
     * Creates an empty precondition.
     */
    public Hl7v2NotEquals() {}

    /**
     * Creates an HL7 v2 precondition with the settings of another precondition.
     * @param notEquals the precondition to copy the settings from
     */
    public Hl7v2NotEquals(NotEquals notEquals) {
        super(notEquals);
    }

    @Override
    public boolean evaluate(ServiceContext context) {
        Hl7v2Equals hl7v2Equals = new Hl7v2Equals(this.getDataPath(), this.getComparisonValue());
        return !hl7v2Equals.evaluate(context);
    }
}
