package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import gov.cdc.izgateway.transformation.configuration.OperationEqualsConfig;
import lombok.extern.java.Log;

import java.util.logging.Level;

@Log
public class Hl7v2EqualsOperation extends BaseConditionalOperation<OperationEqualsConfig> implements ConditionalOperation {

    public Hl7v2EqualsOperation(OperationEqualsConfig configuration) {
        super(configuration);
    }

    @Override
    public boolean evaluate(Message message) throws HL7Exception {
        // TODO - finish implementation

        log.log(Level.WARNING, String.format("Precondition Evaluation: %s / Does %s EQUAL %s",
                this.getClass().getSimpleName(),
                this.operationConfig.getFieldName(),
                this.operationConfig.getFieldValue()));

        Terser terser = new Terser(message);
        String sourceValue = terser.get(operationConfig.getFieldName());

        return (sourceValue.equals(operationConfig.getFieldValue()));
    }
}
