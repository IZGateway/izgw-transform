package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import gov.cdc.izgateway.transformation.configuration.ConditionEmptyConfig;
import gov.cdc.izgateway.transformation.configuration.ConditionNotEmptyConfig;
import lombok.extern.java.Log;

import java.util.logging.Level;

@Log
public class Hl7v2NotEmptyOperation  extends BaseConditionalOperation<ConditionNotEmptyConfig> implements ConditionalOperation {

    public Hl7v2NotEmptyOperation(ConditionNotEmptyConfig configuration) {
        super(configuration);
    }

    @Override
    public boolean evaluate(Message message) throws HL7Exception {

        log.log(Level.WARNING, String.format("Precondition Evaluation: %s / Is %s NOT EMPTY",
                this.getClass().getSimpleName(),
                this.operationConfig.getDataPath()
        ));

        ConditionEmptyConfig emptyConfig = new ConditionEmptyConfig();
        emptyConfig.setDataPath(this.operationConfig.getDataPath());

        Hl7v2EmptyOperation emptyOperation = new Hl7v2EmptyOperation(emptyConfig);

        return !emptyOperation.evaluate(message);

    }
}
