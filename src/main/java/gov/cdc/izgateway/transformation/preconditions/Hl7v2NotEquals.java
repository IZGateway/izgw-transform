package gov.cdc.izgateway.transformation.preconditions;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;

public class Hl7v2NotEquals extends Equals implements Precondition {
    public Hl7v2NotEquals(Equals equals) {
        super(equals);
    }

    @Override
    public boolean evaluate(Message message) throws HL7Exception {
        return false;
    }
}
