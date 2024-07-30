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
    private final List<SolutionOperationService> responseOperations;

    public SolutionService(Solution configuration, DataType dataType) {
        requestOperations = new ArrayList<>();
        responseOperations = new ArrayList<>();

        for (SolutionOperation so : configuration.getRequestOperations()) {
            requestOperations.add(new SolutionOperationService(so, dataType));
        }

        for (SolutionOperation so : configuration.getResponseOperations()) {
            responseOperations.add(new SolutionOperationService(so, dataType));
        }
    }

    // TODO - add Advice logging

    public void execute(ServiceContext context) throws HL7Exception {
        if (context.getCurrentDirection().equals(DataFlowDirection.REQUEST)) {
            for (SolutionOperationService so : requestOperations) {
                so.execute(context);
            }
        } else if (context.getCurrentDirection().equals(DataFlowDirection.RESPONSE)) {
            for (SolutionOperationService so : responseOperations) {
                so.execute(context);
            }
        }
    }
}
