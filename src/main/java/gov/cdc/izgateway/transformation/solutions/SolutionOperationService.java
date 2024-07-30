package gov.cdc.izgateway.transformation.solutions;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.transformation.chains.Hl7v2OperationChain;
import gov.cdc.izgateway.transformation.configuration.*;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataType;
import gov.cdc.izgateway.transformation.model.SolutionOperation;
import gov.cdc.izgateway.transformation.operations.Hl7v2CopyOperation;
import gov.cdc.izgateway.transformation.operations.Hl7v2RegexReplaceOperation;
import gov.cdc.izgateway.transformation.operations.Hl7v2SaveStateOperation;
import gov.cdc.izgateway.transformation.operations.Hl7v2SetOperation;

// TODO - need to finish implementation (preconditions)

public class SolutionOperationService {
    private final Hl7v2OperationChain operations;

    public SolutionOperationService(SolutionOperation solutionOperation, DataType dataType) {
        operations = new Hl7v2OperationChain();

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
            }
        }

    }

    public void execute(ServiceContext context) throws HL7Exception {
        // TODO - add precondition check
        operations.execute(context);
    }
}
