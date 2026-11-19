package gov.cdc.izgateway.xform.util;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

import java.io.IOException;

/**
 * Utility methods for HL7 v2 messages.
 */
public class Hl7Utils {
    /**
     * Creates an instance. All methods are static.
     */
    public Hl7Utils() {
        // No state to initialize.
    }

    /**
     * Appends " - HL7" to a message.
     *
     * @param message the message
     * @return the message with " - HL7" at the end
     */
    public static String getHl7Message(String message) {
        return message + " - HL7";
    }

    /**
     * Parses an HL7 v2 message without validation. Line feeds are changed to carriage returns before parsing.
     *
     * @param rawHl7Message the message in pipe-delimited format
     * @return the parsed message
     * @throws HL7Exception if the message cannot be parsed
     */
    public static Message parseHl7v2Message(String rawHl7Message) throws HL7Exception {
        PipeParser parser;
        DefaultHapiContext context = new DefaultHapiContext();
        rawHl7Message = rawHl7Message.replace("\n", "\r");
        context.setValidationContext(new NoValidation());
        parser = context.getPipeParser();
        return parser.parse(rawHl7Message);
    }
}
