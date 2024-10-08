package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import gov.cdc.izgateway.transformation.configuration.OperationSetConfig;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.exceptions.OperationException;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Hl7v2SetOperation extends BaseOperation<OperationSetConfig> implements Operation {

    public Hl7v2SetOperation(OperationSetConfig config) {
        super(config);
    }
    @Override
    public void thisOperation(ServiceContext context) throws OperationException {

        log.trace(String.format("SET Operation: %s / SET %s TO %s",
                this.getClass().getSimpleName(),
                this.operationConfig.getDestinationField(),
                this.operationConfig.getSetValue()));

        try {
            Message message = context.getCurrentMessage();

            Terser terser = new Terser(message);
            terser.set(operationConfig.getDestinationField(), operationConfig.getSetValue());

            context.setCurrentMessage(message);
        } catch (HL7Exception e) {
            throw new OperationException(e.getMessage(), e.getCause());
        }
    }
}
