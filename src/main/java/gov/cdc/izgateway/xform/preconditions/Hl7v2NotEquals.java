package gov.cdc.izgateway.xform.preconditions;

import gov.cdc.izgateway.xform.context.ServiceContext;

public class Hl7v2NotEquals extends NotEquals implements Precondition {
    public Hl7v2NotEquals() {}

    public Hl7v2NotEquals(NotEquals notEquals) {
        super(notEquals);
    }

    @Override
    public boolean evaluate(ServiceContext context) {
        Hl7v2Equals hl7v2Equals = new Hl7v2Equals(this.getDataPath(), this.getComparisonValue());
        return !hl7v2Equals.evaluate(context);
    }
}
