package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import gov.cdc.izgateway.transformation.configuration.OperationSaveStateConfig;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Hl7v2SaveStateOperation extends BaseOperation<OperationSaveStateConfig> implements Operation {
    public Hl7v2SaveStateOperation(OperationSaveStateConfig config) {
        super(config);
    }

    @Override
    public void thisOperation(ServiceContext context) throws HL7Exception {
        log.trace(String.format("Operation: %s / Save value from %s To key %s",
                this.getClass().getSimpleName(),
                this.operationConfig.getField(),
                this.operationConfig.getKey()));

        Message message = context.getCurrentMessage();

        Terser terser = new Terser(message);

        String sourceValue = terser.get(operationConfig.getField());
        context.getState().put(operationConfig.getKey(), sourceValue);
    }
}
