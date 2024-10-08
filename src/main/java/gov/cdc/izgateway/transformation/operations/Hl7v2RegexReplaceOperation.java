package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import gov.cdc.izgateway.transformation.configuration.OperationRegexReplaceConfig;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.exceptions.OperationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Hl7v2RegexReplaceOperation extends BaseOperation<OperationRegexReplaceConfig> implements Operation {

    public Hl7v2RegexReplaceOperation(OperationRegexReplaceConfig config) {
        super(config);

    }
    @Override
    public void thisOperation(ServiceContext context) throws OperationException {

        log.trace(String.format("Operation: %s on '%s' with regex '%s' and replacement '%s'",
                this.getClass().getSimpleName(),
                this.operationConfig.getField(),
                this.operationConfig.getRegex(),
                this.operationConfig.getReplacement()));

        try {
            Message message = context.getCurrentMessage();

            Terser terser = new Terser(message);

            String sourceValue = terser.get(operationConfig.getField());
            if (sourceValue != null) {
                String cleanedValue = sourceValue.replaceAll(operationConfig.getRegex(), operationConfig.getReplacement());
                terser.set(operationConfig.getField(), cleanedValue);

                context.setCurrentMessage(message);
            }
        } catch (HL7Exception ex) {
            throw new OperationException(ex.getMessage(), ex.getCause());
        }
        
    }
}

