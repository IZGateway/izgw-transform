package gov.cdc.izgateway.transformation.chains;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.operations.Operation;

public class Hl7v2OperationChain {
    private Operation firstOperation;

    public void addOperation(Operation operation) {
        if (firstOperation == null) {
            firstOperation = operation;
        } else {
            Operation currentOperation = firstOperation;
            while (currentOperation.getNextOperation() != null) {
                currentOperation = currentOperation.getNextOperation();
            }
            currentOperation.setNextOperation(operation);
        }
    }

    public void execute(Message message) throws HL7Exception {
        if (firstOperation != null) {
            firstOperation.transform(message);
        }
    }

    public void newExecute(ServiceContext context) throws HL7Exception {
        if (firstOperation != null) {
            firstOperation.execute(context);
        }
    }
}
