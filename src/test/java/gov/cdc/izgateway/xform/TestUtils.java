package gov.cdc.izgateway.xform;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import gov.cdc.izgateway.xform.operations.Copy;
import gov.cdc.izgateway.xform.operations.RegexReplace;
import gov.cdc.izgateway.xform.operations.Set;

/**
 * Helper methods that create operations and HL7 messages for tests.
 */
public class TestUtils {

    /**
     * Creates an instance. This class contains only static methods.
     */
    public TestUtils() {
    }

    /**
     * Creates a copy operation with order 0.
     *
     * @param sourceField the field to copy from
     * @param destinationField the field to copy to
     * @return the copy operation
     */
    public static Copy getCopyOperation(String sourceField, String destinationField) {
        Copy copyBase = new Copy();
        copyBase.setOrder(0);
        copyBase.setSourceField(sourceField);
        copyBase.setDestinationField(destinationField);
        return copyBase;
    }

    /**
     * Creates a set operation with order 0.
     *
     * @param destinationField the field to set
     * @param setValue the value to set
     * @return the set operation
     */
    public static Set getSetOperation(String destinationField, String setValue) {
        Set setBase = new Set();
        setBase.setOrder(0);
        setBase.setDestinationField(destinationField);
        setBase.setSetValue(setValue);
        return setBase;
    }

    /**
     * Creates a regex replace operation with order 0.
     *
     * @param sourceField the field to change
     * @param regex the regular expression to find
     * @param replacement the replacement text
     * @return the regex replace operation
     */
    public static RegexReplace getRegexOperation(String sourceField, String regex, String replacement) {
        RegexReplace regexBase = new RegexReplace();
        regexBase.setOrder(0);
        regexBase.setField(sourceField);
        regexBase.setRegex(regex);
        regexBase.setReplacement(replacement);
        return regexBase;
    }

    /**
     * Parses an HL7 v2 message without validation and encodes it again.
     *
     * @param hl7String the HL7 v2 message
     * @return the encoded message
     * @throws HL7Exception if the message cannot be parsed or encoded
     */
    public static String getEncodedHl7FromString(String hl7String) throws HL7Exception{

        DefaultHapiContext context = new DefaultHapiContext();
        context.setValidationContext(new NoValidation());
        PipeParser parser = context.getPipeParser();
        Message expectedMessage = parser.parse(hl7String);
        return expectedMessage.encode();
    }
}
