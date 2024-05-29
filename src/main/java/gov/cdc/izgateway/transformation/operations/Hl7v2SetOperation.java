package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import gov.cdc.izgateway.transformation.configuration.OperationSetConfig;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import java.util.logging.Level;

@Slf4j
public class Hl7v2SetOperation extends BaseOperation<OperationSetConfig> implements Operation {

    public Hl7v2SetOperation(OperationSetConfig config) {
        super(config);
    }
    @Override
    public void executeOperation(Message message) throws HL7Exception {

        log.trace(String.format("SET Operation: %s / SET %s TO %s",
                this.getClass().getSimpleName(),
                this.operationConfig.getDestinationField(),
                this.operationConfig.getSetValue()));

        Terser terser = new Terser(message);
        terser.set(operationConfig.getDestinationField(), operationConfig.getSetValue());
    }
}
