package gov.cdc.izgateway.transformation.solutions;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.chains.Hl7v2OperationChain;
import gov.cdc.izgateway.transformation.configuration.*;
import gov.cdc.izgateway.transformation.operations.ConditionalOperation;
import gov.cdc.izgateway.transformation.operations.Hl7v2CopyOperation;
import gov.cdc.izgateway.transformation.operations.Hl7v2EqualsOperation;
import gov.cdc.izgateway.transformation.operations.Hl7v2SetOperation;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Log
public class SolutionOperations {
    private final Hl7v2OperationChain operations;
    private final List<ConditionalOperation> preconditions;

    public SolutionOperations(List<SolutionOperationsConfig> configuration) {
        operations = new Hl7v2OperationChain();

        // TODO add preconditions
        preconditions = new ArrayList<ConditionalOperation>();

        for (SolutionOperationsConfig solutionOperationsConfig : configuration) {

            for (OperationConfig conditionalConfig : solutionOperationsConfig.getPreconditions()) {
                if (conditionalConfig instanceof OperationEqualsConfig operationsEqualsConfig) {
                    preconditions.add(new Hl7v2EqualsOperation(operationsEqualsConfig));
                }
            }

            for (OperationConfig operationConfig : solutionOperationsConfig.getOperationList()) {
                // I hate this so much there has to be a better way
                if (operationConfig instanceof OperationCopyConfig operationCopyConfig) {
                    operations.addOperation(new Hl7v2CopyOperation(operationCopyConfig));
                } else if (operationConfig instanceof OperationSetConfig operationSetConfig) {
                    operations.addOperation(new Hl7v2SetOperation(operationSetConfig));
                }
            }

        }

    }

    // TODO - make generic not HL7 specific
    // TODO - pass a context or state around to determine if we are running request or response
    public void execute(Message message) throws HL7Exception {
        if (preconditionPass(message)) {
            log.log(Level.WARNING, "Precondition Passed");
            operations.execute(message);
        } else {
            log.log(Level.WARNING, "Precondition Failed");
        }
    }

    private boolean preconditionPass(Message message) throws HL7Exception {
        boolean pass = true;

        for (ConditionalOperation op : preconditions) {
            pass = pass && op.evaluate(message);
        }

        return pass;
    }
}
