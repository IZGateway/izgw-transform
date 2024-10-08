package gov.cdc.izgateway.transformation;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import gov.cdc.izgateway.transformation.configuration.OperationRegexReplaceConfig;
import gov.cdc.izgateway.transformation.configuration.OperationSetConfig;
import gov.cdc.izgateway.transformation.operations.*;

public class TestUtils {

    public static Copy getCopyOperation(String sourceField, String destinationField) {
        Copy copyBase = new Copy();
        copyBase.setSourceField(sourceField);
        copyBase.setDestinationField(destinationField);
        return copyBase;
    }

    public static Hl7v2SetOperation getSetOperation(String destinationField, String setValue) {
        OperationSetConfig config = new OperationSetConfig();
        config.setDestinationField(destinationField);
        config.setSetValue(setValue);
        return new Hl7v2SetOperation(config);
    }

    public static Hl7v2RegexReplaceOperation getRegexOperation(String sourceField, String regex, String replacement) {
        OperationRegexReplaceConfig config = new OperationRegexReplaceConfig();
        config.setField(sourceField);
        config.setRegex(regex);
        config.setReplacement(replacement);
        return new Hl7v2RegexReplaceOperation(config);
     }

    public static String getEncodedHl7FromString(String hl7String) throws HL7Exception{

        DefaultHapiContext context = new DefaultHapiContext();
        context.setValidationContext(new NoValidation());
        PipeParser parser = context.getPipeParser();
        Message expectedMessage = parser.parse(hl7String);
        return expectedMessage.encode();
    }
}
