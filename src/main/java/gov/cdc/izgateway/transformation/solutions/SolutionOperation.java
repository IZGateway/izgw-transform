package gov.cdc.izgateway.transformation.solutions;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.chains.Hl7v2OperationChain;
import gov.cdc.izgateway.transformation.configuration.*;
import gov.cdc.izgateway.transformation.operations.*;
import gov.cdc.izgateway.transformation.preconditions.Precondition;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Log
public class SolutionOperation {

    private final Hl7v2OperationChain operations;
    private final List<Precondition> preconditions;

    public SolutionOperation(SolutionOperationsConfig configuration) {
        operations = new Hl7v2OperationChain();

        preconditions = new ArrayList<>();

        for (Precondition precondition : configuration.getPreconditions()) {
            preconditions.add(precondition);

//            if (conditionalConfig instanceof ConditionEqualsConfig operationsEqualsConfig) {
//                preconditions.add(new Hl7v2EqualsOperation(operationsEqualsConfig));
//            }
//
//            else if (conditionalConfig instanceof ConditionNotEqualsConfig operationsNotEqualsConfig) {
//                preconditions.add(new Hl7v2NotEqualsOperation(operationsNotEqualsConfig));
//            }
//
//            else if (conditionalConfig instanceof ConditionNotEmptyConfig conditionNotEmptyConfig) {
//                preconditions.add(new Hl7v2NotEmptyOperation(conditionNotEmptyConfig));
//            }
//
//            else if (conditionalConfig instanceof ConditionEmptyConfig conditionEmptyConfig) {
//                preconditions.add(new Hl7v2EmptyOperation(conditionEmptyConfig));
//            }
        }

        for (OperationConfig operationConfig : configuration.getOperationList()) {
            // I hate this so much there has to be a better way
            if (operationConfig instanceof OperationCopyConfig operationCopyConfig) {
                operations.addOperation(new Hl7v2CopyOperation(operationCopyConfig));
            } else if (operationConfig instanceof OperationSetConfig operationSetConfig) {
                operations.addOperation(new Hl7v2SetOperation(operationSetConfig));
            }
        }

    }

    private boolean preconditionPass(Message message) throws HL7Exception {
        boolean pass = true;

        for (Precondition op : preconditions) {
            pass = pass && op.evaluate(message);
        }

        return pass;
    }

    // TODO - make generic not HL7 specific
    public void execute(Message message) throws HL7Exception {

        if (preconditionPass(message)) {
            log.log(Level.WARNING, "Precondition Passed");
            operations.execute(message);
        } else {
            log.log(Level.WARNING, "Precondition Failed");
        }

    }
}
