package gov.cdc.izgateway.transformation.transformers;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;

public interface DataTransformation {
    void execute(Message message) throws HL7Exception;
}
