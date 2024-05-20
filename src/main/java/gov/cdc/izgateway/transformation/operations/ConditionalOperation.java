package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;

public interface ConditionalOperation {
    boolean evaluate(Message message) throws HL7Exception;
}
