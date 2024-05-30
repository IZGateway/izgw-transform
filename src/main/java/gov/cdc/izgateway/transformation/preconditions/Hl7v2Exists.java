package gov.cdc.izgateway.transformation.preconditions;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;

public class Hl7v2Exists extends Exists implements Precondition {

    public Hl7v2Exists() {
        super();
    }

    public Hl7v2Exists(String fieldName) {
        super(fieldName);
    }

    @Override
    public boolean evaluate(Message message) {
        Terser terser = new Terser(message);

        String pathValue;
        try {
            pathValue = terser.get(this.getDataPath());
        } catch (HL7Exception e) {
            // HL7Exception will happen if Terser.get references Segment that does not exist in the message
            // In this case just returning false.  Entirely possible that Analyst/Engineer has a precondition
            // set for a segment that just simply won't exist in every message submitted.
            return false;
        }

        return pathValue != null;

    }
}
