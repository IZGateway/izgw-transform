package gov.cdc.izgateway.transformation;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import gov.cdc.izgateway.transformation.operations.*;

public class TestUtils {

    public static Copy getCopyOperation(String sourceField, String destinationField) {
        Copy copyBase = new Copy();
        copyBase.setSourceField(sourceField);
        copyBase.setDestinationField(destinationField);
        return copyBase;
    }

    public static Set getSetOperation(String destinationField, String setValue) {
        Set setBase = new Set();
        setBase.setDestinationField(destinationField);
        setBase.setSetValue(setValue);
        return setBase;
    }

    public static RegexReplace getRegexOperation(String sourceField, String regex, String replacement) {
        RegexReplace regexBase = new RegexReplace();
        regexBase.setField(sourceField);
        regexBase.setRegex(regex);
        regexBase.setReplacement(replacement);
        return regexBase;
    }

    public static String getEncodedHl7FromString(String hl7String) throws HL7Exception{

        DefaultHapiContext context = new DefaultHapiContext();
        context.setValidationContext(new NoValidation());
        PipeParser parser = context.getPipeParser();
        Message expectedMessage = parser.parse(hl7String);
        return expectedMessage.encode();
    }
}
