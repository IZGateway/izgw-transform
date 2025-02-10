package gov.cdc.izgateway.xform;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import gov.cdc.izgateway.xform.operations.Copy;
import gov.cdc.izgateway.xform.operations.RegexReplace;
import gov.cdc.izgateway.xform.operations.Set;

public class TestUtils {

    public static Copy getCopyOperation(String sourceField, String destinationField) {
        Copy copyBase = new Copy();
        copyBase.setOrder(0);
        copyBase.setSourceField(sourceField);
        copyBase.setDestinationField(destinationField);
        return copyBase;
    }

    public static Set getSetOperation(String destinationField, String setValue) {
        Set setBase = new Set();
        setBase.setOrder(0);
        setBase.setDestinationField(destinationField);
        setBase.setSetValue(setValue);
        return setBase;
    }

    public static RegexReplace getRegexOperation(String sourceField, String regex, String replacement) {
        RegexReplace regexBase = new RegexReplace();
        regexBase.setOrder(0);
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
