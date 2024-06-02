package gov.cdc.izgateway.transformation.solutions;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.chains.Hl7v2OperationChain;
import gov.cdc.izgateway.transformation.configuration.*;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataType;
import gov.cdc.izgateway.transformation.operations.*;
import gov.cdc.izgateway.transformation.preconditions.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SolutionOperation {

    private final Hl7v2OperationChain operations;
    private final List<Precondition> preconditions;

    public SolutionOperation(SolutionOperationsConfig configuration, DataType dataType) {
        operations = new Hl7v2OperationChain();

        preconditions = new ArrayList<>();

        for (Precondition precondition : configuration.getPreconditions()) {
            if (dataType.equals(DataType.HL7V2) && precondition.getClass().equals(Equals.class)) {
                preconditions.add(new Hl7v2Equals((Equals) precondition));
            } else if (dataType.equals(DataType.HL7V2) && precondition.getClass().equals(NotEquals.class)) {
                preconditions.add(new Hl7v2NotEquals((NotEquals) precondition));
            } else if (dataType.equals(DataType.HL7V2) && precondition.getClass().equals(Exists.class)) {
                preconditions.add(new Hl7v2Exists((Exists) precondition));
            } else if (dataType.equals(DataType.HL7V2) && precondition.getClass().equals(NotExists.class)) {
                preconditions.add(new Hl7v2NotExists((NotExists) precondition));
            } else if (dataType.equals(DataType.HL7V2) && precondition.getClass().equals(RegexMatch.class)) {
                preconditions.add(new Hl7v2RegexMatch((RegexMatch) precondition));
            }
        }

        for (OperationConfig operationConfig : configuration.getOperationList()) {
            // I hate this so much there has to be a better way
            if (operationConfig instanceof OperationCopyConfig operationCopyConfig) {
                operations.addOperation(new Hl7v2CopyOperation(operationCopyConfig));
            } else if (operationConfig instanceof OperationSetConfig operationSetConfig) {
                operations.addOperation(new Hl7v2SetOperation(operationSetConfig));
            } else if (operationConfig instanceof OperationRegexReplaceConfig operationRegexReplaceConfig) {
                operations.addOperation(new Hl7v2RegexReplaceOperation(operationRegexReplaceConfig));
            }
        }

    }

    private boolean preconditionPass(Message message) {
        boolean pass = true;

        for (Precondition op : preconditions) {
            pass = pass && op.evaluate(message);
        }

        return pass;
    }

    // TODO - make generic not HL7 specific
    public void execute(ServiceContext context) throws HL7Exception {

        if (preconditionPass(context.getRequestMessage())) {
            log.trace("Solution Operation Precondition Passed");
            operations.execute(context);
        } else {
            log.trace("Solution Operation Precondition Failed");
        }
    }
}
