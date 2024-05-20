package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.configuration.ConditionEqualsConfig;
import gov.cdc.izgateway.transformation.configuration.ConditionNotEqualsConfig;
import lombok.extern.java.Log;

import java.util.logging.Level;

@Log
public class Hl7v2NotEqualsOperation extends BaseConditionalOperation<ConditionNotEqualsConfig> implements ConditionalOperation {
    public Hl7v2NotEqualsOperation(ConditionNotEqualsConfig configuration) {
        super(configuration);
    }

    @Override
    public boolean evaluate(Message message) throws HL7Exception {

        log.log(Level.WARNING, String.format("Precondition Evaluation: %s / Does %s NOT EQUAL %s",
                this.getClass().getSimpleName(),
                this.operationConfig.getFieldName(),
                this.operationConfig.getFieldValue()));

        ConditionEqualsConfig equalsConfig = new ConditionEqualsConfig();
        equalsConfig.setFieldName(operationConfig.getFieldName());
        equalsConfig.setFieldValue(operationConfig.getFieldValue());
        Hl7v2EqualsOperation equalsOperation = new Hl7v2EqualsOperation(equalsConfig);
        return !equalsOperation.evaluate(message);
    }
}
