package gov.cdc.izgateway.xform.util;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

import java.io.IOException;

public class Hl7Utils {
    public static String getHl7Message(String message) {
        return message + " - HL7";
    }

    public static Message parseHl7v2Message(String rawHl7Message) throws HL7Exception {
        PipeParser parser;
        DefaultHapiContext context = new DefaultHapiContext();
        rawHl7Message = rawHl7Message.replace("\n", "\r");
        context.setValidationContext(new NoValidation());
        parser = context.getPipeParser();
        return parser.parse(rawHl7Message);
    }
}
