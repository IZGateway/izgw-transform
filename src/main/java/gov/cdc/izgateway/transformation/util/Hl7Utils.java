package gov.cdc.izgateway.transformation.util;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

import java.io.IOException;
import java.util.UUID;

public class Hl7Utils {
    public static String getHl7Message(String message) {
        return message + " - HL7";
    }

    public static Message parseHl7v2Message(String rawHl7Message) throws HL7Exception {
        PipeParser parser;
        try (DefaultHapiContext context = new DefaultHapiContext()) {
            rawHl7Message = rawHl7Message.replace("\n", "\r");
            context.setValidationContext(new NoValidation());
            parser = context.getPipeParser();
        }

        return parser.parse(rawHl7Message);
    }

    // TODO: This is temporary until we discuss this.
    public static UUID getOrganizationId(String organization) {

        switch (organization) {
            case "IZG":
                return UUID.fromString("0d15449b-fb08-4013-8985-20c148b353fe");
        }

        return null;
    }

}
