package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import gov.cdc.izgateway.transformation.configuration.OperationRegexReplaceConfig;
import lombok.extern.java.Log;

import java.util.logging.Level;

@Log
public class Hl7v2RegexReplaceOperation extends BaseOperation<OperationRegexReplaceConfig> implements Operation {

    public Hl7v2RegexReplaceOperation(OperationRegexReplaceConfig config) {
        super(config);

    }
    @Override
    public void executeOperation(Message message) throws HL7Exception {

        log.log(Level.WARNING, String.format("COPY Operation: %s / Copy %s TO %s",
                this.getClass().getSimpleName(),
                this.operationConfig.getField(),
                this.operationConfig.getReplacement(),
                this.operationConfig.getRegex()));

        Terser terser = new Terser(message);

        String sourceValue = terser.get(operationConfig.getField());
        if(sourceValue != null){
            String cleanedValue = sourceValue.replaceAll(operationConfig.getRegex(), operationConfig.getReplacement());
            terser.set(operationConfig.getField(), cleanedValue);
        }
        
    }
}

