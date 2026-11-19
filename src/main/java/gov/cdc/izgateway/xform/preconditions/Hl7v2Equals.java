package gov.cdc.izgateway.xform.preconditions;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import gov.cdc.izgateway.xform.context.ServiceContext;

/**
 * HL7 v2 implementation of the {@link Equals} precondition.
 */
public class Hl7v2Equals extends Equals implements Precondition {

    /**
     * Creates an empty precondition.
     */
    public Hl7v2Equals() {}

    /**
     * Creates a precondition that compares a message field to a value.
     * @param dataPath the HL7 v2 Terser path of the field
     * @param comparisonValue the value to compare the field to
     */
    public Hl7v2Equals(String dataPath, String comparisonValue) {
        super(dataPath, comparisonValue);
    }

    /**
     * Creates an HL7 v2 precondition with the settings of another precondition.
     * @param equals the precondition to copy the settings from
     */
    public Hl7v2Equals(Equals equals) {
        super(equals);
    }

    @Override
    public boolean evaluate(ServiceContext context) {

        Message message = context.getCurrentMessage();
        // TODO: If IZGW returns a fault message, message will be null.
        // We need to figure out how to handle that
        if (message == null) {
        	return false;
        }
        Terser terser = new Terser(message);

        String sourceValue;
        try {
            sourceValue = terser.get(this.getDataPath());
        } catch (HL7Exception e) {
            // HL7Exception will happen if Terser.get references Segment that does not exist in the message
            // In this case just returning false.  Entirely possible that Analyst/Engineer has a precondition
            // set for a segment that just simply won't exist in every message submitted.
            return false;
        }

        if (sourceValue == null) {
            return false;
        }

        return (sourceValue.equals(this.getComparisonValue()));

    }
}
