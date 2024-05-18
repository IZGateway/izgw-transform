package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import gov.cdc.izgateway.transformation.configuration.ConditionEmptyConfig;
import lombok.extern.java.Log;

import java.util.logging.Level;

@Log
public class Hl7v2EmptyOperation extends BaseConditionalOperation<ConditionEmptyConfig> implements ConditionalOperation {
    public Hl7v2EmptyOperation(ConditionEmptyConfig configuration) {
        super(configuration);
    }

    @Override
    public boolean evaluate(Message message) throws HL7Exception {
        // TODO - think through implementation more.  Right now assumes path is valid HAPI Terser path
        //        which will return a null if the field is empty or just does not exist in the message.

        log.log(Level.WARNING, String.format("Precondition Evaluation: %s / Is %s EMPTY",
                this.getClass().getSimpleName(),
                this.operationConfig.getDataPath()
        ));

        Terser terser = new Terser(message);
        String sourceValue = terser.get(operationConfig.getDataPath());

        return sourceValue == null || sourceValue.isEmpty();
    }
}
