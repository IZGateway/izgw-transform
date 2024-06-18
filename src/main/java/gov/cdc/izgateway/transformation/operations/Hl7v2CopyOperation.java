package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import gov.cdc.izgateway.transformation.configuration.OperationCopyConfig;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Hl7v2CopyOperation extends BaseOperation<OperationCopyConfig> implements Operation {

    public Hl7v2CopyOperation(OperationCopyConfig config) {
        super(config);
    }

    @Override
    public void thisOperation(ServiceContext context) throws HL7Exception {
        log.trace(String.format("COPY Operation: %s / Copy %s TO %s",
                this.getClass().getSimpleName(),
                this.operationConfig.getSourceField(),
                this.operationConfig.getDestinationField()));

        Message message = context.getCurrentMessage();

        Terser terser = new Terser(message);

        String sourceValue = terser.get(operationConfig.getSourceField());
        terser.set(operationConfig.getDestinationField(), sourceValue);

        context.setCurrentMessage(message);
    }
}
