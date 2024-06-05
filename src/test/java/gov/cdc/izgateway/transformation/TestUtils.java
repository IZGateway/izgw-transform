package gov.cdc.izgateway.transformation;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import gov.cdc.izgateway.transformation.configuration.OperationCopyConfig;
import gov.cdc.izgateway.transformation.configuration.OperationSetConfig;
import gov.cdc.izgateway.transformation.operations.Hl7v2CopyOperation;
import gov.cdc.izgateway.transformation.operations.Hl7v2SetOperation;

public class TestUtils {

    public static Hl7v2CopyOperation getCopyOperation(String sourceField, String destinationField) {
        OperationCopyConfig config = new OperationCopyConfig();
        config.setSourceField(sourceField);
        config.setDestinationField(destinationField);
        return new Hl7v2CopyOperation(config);
    }

    public static Hl7v2SetOperation getSetOperation(String destinationField, String setValue) {
        OperationSetConfig config = new OperationSetConfig();
        config.setDestinationField(destinationField);
        config.setSetValue(setValue);
        return new Hl7v2SetOperation(config);
    }

    public static String getEncodedHl7FromString(String hl7String) throws HL7Exception{

        DefaultHapiContext context = new DefaultHapiContext();
        context.setValidationContext(new NoValidation());
        PipeParser parser = context.getPipeParser();
        Message expectedMessage = parser.parse(hl7String);
        return expectedMessage.encode();
    }
}
