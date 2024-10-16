package gov.cdc.izgateway.transformation.solutions;

import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.exceptions.OperationException;
import gov.cdc.izgateway.transformation.exceptions.SolutionOperationException;
import gov.cdc.izgateway.transformation.operations.*;
import gov.cdc.izgateway.transformation.preconditions.*;

import java.util.List;

public class SolutionOperation {
    private final List<Precondition> preconditions;
    private final List<Operation> operations;

    public SolutionOperation(gov.cdc.izgateway.transformation.model.SolutionOperation solutionOperation) {
        preconditions = solutionOperation.getPreconditions();
        operations = solutionOperation.getOperationList();
    }

    private boolean passedPreconditions(ServiceContext context) {
        boolean passed = true;

        for (Precondition op : preconditions) {
            passed = passed && op.evaluate(context);
        }

        return passed;
    }

    public void execute(ServiceContext context) throws SolutionOperationException {

        if (passedPreconditions(context)) {

            for (Operation op : operations) {
                try {
                    op.execute(context);
                } catch (OperationException e) {
                    throw new SolutionOperationException(
                            String.format("Failed to execute operation: %s - %s",
                                    op.getClass().getSimpleName(),
                                    e.getMessage()),
                            e.getCause());
                }
            }
        }
    }
}
