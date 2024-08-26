package gov.cdc.izgateway.transformation.preconditions;

import gov.cdc.izgateway.transformation.context.ServiceContext;

public class Hl7v2NotExists extends NotExists implements Precondition {

    public Hl7v2NotExists(NotExists notExists) {
        super(notExists);
    }

    public Hl7v2NotExists(String dataPath) {
        super(dataPath);
    }

    @Override
    public boolean evaluate(ServiceContext context) {
        Hl7v2Exists exists = new Hl7v2Exists(this.getDataPath());
        return !exists.evaluate(context);
    }
}
