package gov.cdc.izgateway.transformation.transformers;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.chains.Hl7v2OperationChain;
import gov.cdc.izgateway.transformation.configuration.*;
import gov.cdc.izgateway.transformation.operations.ConditionalOperation;
import gov.cdc.izgateway.transformation.operations.Hl7v2CopyOperation;
import gov.cdc.izgateway.transformation.operations.Hl7v2EqualsOperation;
import gov.cdc.izgateway.transformation.operations.Hl7v2SetOperation;
import lombok.extern.java.Log;

import java.util.logging.Level;

@Log
public class Hl7DataTransformation extends BaseDataTransformation implements DataTransformation {
    private ConditionalOperation precondition;
    private final Hl7v2OperationChain operationChain;

    public Hl7DataTransformation(DataTransformationConfig dataTransformationConfig) {
        super(dataTransformationConfig);

        // Precondition
        // TODO - this maybe needs to be a list?
        // TODO - will have to account for other "types" of precondition operations (not equals, etc...)
        if (configuration.getPrecondition() instanceof OperationEqualsConfig operationEqualsConfig) {
            precondition = new Hl7v2EqualsOperation(operationEqualsConfig);
        }

        // Operation(s)
        // TODO - replace with Chain Builder?
        operationChain = new Hl7v2OperationChain();
        for (OperationConfig operationConfig : configuration.getOperationList()) {
            // I hate this so much there has to be a better way
            if (operationConfig instanceof OperationCopyConfig operationCopyConfig) {
                operationChain.addOperation(new Hl7v2CopyOperation(operationCopyConfig));
            } else if (operationConfig instanceof OperationSetConfig operationSetConfig) {
                operationChain.addOperation(new Hl7v2SetOperation(operationSetConfig));
            }
        }


    }

    @Override
    public void execute(Message message) throws HL7Exception {
        // TODO - finish implementing this (maybe :) )


        // TODO -execute precondition
        if (precondition.evaluate(message)) {
            log.log(Level.WARNING, "Precondition Passed");
            operationChain.execute(message);
        } else {
            log.log(Level.WARNING, "Precondition Failed");
        }

    }
}
