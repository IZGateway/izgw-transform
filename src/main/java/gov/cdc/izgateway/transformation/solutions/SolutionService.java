package gov.cdc.izgateway.transformation.solutions;

// To repalce Solution as we refactor to pull from different files.
// TODO - rename this ugh

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.enums.DataType;
import gov.cdc.izgateway.transformation.model.Solution;
import gov.cdc.izgateway.transformation.model.SolutionOperation;

import java.util.ArrayList;
import java.util.List;

public class SolutionService {

    private final List<SolutionOperationService> requestOperations;

    public SolutionService(Solution configuration, DataType dataType) {
        requestOperations = new ArrayList<>();

        for (SolutionOperation so : configuration.getRequestOperations()) {
            requestOperations.add(new SolutionOperationService(so, dataType));
        }

    }

    // TODO finish implementation by looking at Solution.java
    // TODO - add Advice logging

    public void execute(ServiceContext context) throws HL7Exception {
        if (context.getCurrentDirection().equals(DataFlowDirection.REQUEST)) {
            for (SolutionOperationService so : requestOperations) {
                //so.execute(context);
            }
        }
    }
}
