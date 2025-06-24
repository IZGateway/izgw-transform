package gov.cdc.izgateway.xform.solutions;

import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.exceptions.OperationException;
import gov.cdc.izgateway.xform.exceptions.SolutionOperationException;
import gov.cdc.izgateway.xform.operations.Operation;
import gov.cdc.izgateway.xform.preconditions.Precondition;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SolutionOperation {
    private final List<Precondition> preconditions;
    private final List<Operation> operations;

    public SolutionOperation(gov.cdc.izgateway.xform.model.SolutionOperation solutionOperation) {
        preconditions = Objects.requireNonNullElse(solutionOperation.getPreconditions(), Collections.emptyList());
        operations = Objects.requireNonNullElse(solutionOperation.getOperationList(), Collections.emptyList());
        // Ensure that SolutionOperation list is sorted by order
        Collections.sort(operations, this::compareByOrder);
    }

	private int compareByOrder(Operation o1, Operation o2) {
		if (o1 == o2) {
			return 0;
		}
		if (o2 == null) {
			return 1;
		}
		if (o1 == null) {
			return -1;
		}
		int order1 = Objects.requireNonNullElse(o1.getOrder(), Integer.valueOf(0));
		int order2 = Objects.requireNonNullElse(o2.getOrder(), Integer.valueOf(0));
		return Integer.compare(order1, order2);
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
