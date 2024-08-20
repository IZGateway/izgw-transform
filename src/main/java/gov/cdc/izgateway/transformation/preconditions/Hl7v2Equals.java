package gov.cdc.izgateway.transformation.preconditions;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageRequest;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageResponse;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.context.XformContext;

public class Hl7v2Equals extends Equals implements Precondition {

    public Hl7v2Equals() {}

    public Hl7v2Equals(String dataPath, String comparisonValue) {
        super(dataPath, comparisonValue);
    }

    public Hl7v2Equals(Equals equals) {
        super(equals);
    }

    @Override
    public boolean evaluate(XformContext<SubmitSingleMessageRequest, SubmitSingleMessageResponse> context) {
        return false;
//        Message message = context.getRequestMessage().getHl7Message();
//        Terser terser = new Terser(message);
//
//        String sourceValue;
//        try {
//            sourceValue = terser.get(this.getDataPath());
//        } catch (HL7Exception e) {
//            // HL7Exception will happen if Terser.get references Segment that does not exist in the message
//            // In this case just returning false.  Entirely possible that Analyst/Engineer has a precondition
//            // set for a segment that just simply won't exist in every message submitted.
//            return false;
//        }
//
//        if (sourceValue == null) {
//            return false;
//        }
//
//        return (sourceValue.equals(this.getComparisonValue()));
    }

    @Override
    public boolean evaluate(ServiceContext context) {

        Message message = context.getCurrentMessage();
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
