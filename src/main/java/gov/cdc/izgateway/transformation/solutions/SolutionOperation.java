package gov.cdc.izgateway.transformation.solutions;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.transformation.chains.Hl7v2OperationChain;
import gov.cdc.izgateway.transformation.configuration.*;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.operations.*;
import gov.cdc.izgateway.transformation.preconditions.*;

import java.util.List;

public class SolutionOperation {
    private final Hl7v2OperationChain operations;
    private final List<Precondition> preconditions;

    public SolutionOperation(gov.cdc.izgateway.transformation.model.SolutionOperation solutionOperation) {
        operations = new Hl7v2OperationChain();
        preconditions = solutionOperation.getPreconditions();

        for (OperationConfig operationConfig : solutionOperation.getOperationList()) {
            // I hate this so much there has to be a better way
            if (operationConfig instanceof OperationCopyConfig operationCopyConfig) {
                operations.addOperation(new Hl7v2CopyOperation(operationCopyConfig));
            } else if (operationConfig instanceof OperationSetConfig operationSetConfig) {
                operations.addOperation(new Hl7v2SetOperation(operationSetConfig));
            } else if (operationConfig instanceof OperationRegexReplaceConfig operationRegexReplaceConfig) {
                operations.addOperation(new Hl7v2RegexReplaceOperation(operationRegexReplaceConfig));
            } else if (operationConfig instanceof OperationSaveStateConfig operationSaveStateConfig) {
                operations.addOperation(new Hl7v2SaveStateOperation(operationSaveStateConfig));
            }  else if (operationConfig instanceof OperationMapperConfig operationMapperConfig) {
                operations.addOperation(new Hl7v2MapOperation(operationMapperConfig));
            }
        }

    }

    private boolean passedPreconditions(ServiceContext context) {
        boolean passed = true;

        for (Precondition op : preconditions) {
            passed = passed && op.evaluate(context);
        }

        return passed;
    }

    public void execute(ServiceContext context) throws HL7Exception {
        if (passedPreconditions(context)) operations.execute(context);
    }
}
