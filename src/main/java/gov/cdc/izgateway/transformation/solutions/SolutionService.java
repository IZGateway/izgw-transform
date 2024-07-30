package gov.cdc.izgateway.transformation.solutions;

// To replace Solution as we refactor to pull from different files.
// TODO - rename this ugh

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.enums.DataType;
import gov.cdc.izgateway.transformation.logging.advice.Advisable;
import gov.cdc.izgateway.transformation.model.Solution;
import gov.cdc.izgateway.transformation.model.SolutionOperation;

import java.util.ArrayList;
import java.util.List;

public class SolutionService implements Advisable {

    private final Solution configuration;
    private final List<SolutionOperationService> requestOperations;
    private final List<SolutionOperationService> responseOperations;
    private boolean hasTransformed = false;

    public SolutionService(Solution configuration, DataType dataType) {
        this.configuration = configuration;
        requestOperations = new ArrayList<>();
        responseOperations = new ArrayList<>();

        for (SolutionOperation so : configuration.getRequestOperations()) {
            requestOperations.add(new SolutionOperationService(so, dataType));
        }

        for (SolutionOperation so : configuration.getResponseOperations()) {
            responseOperations.add(new SolutionOperationService(so, dataType));
        }
    }

    public void execute(ServiceContext context) throws HL7Exception {
        if (context.getCurrentDirection().equals(DataFlowDirection.REQUEST)) {
            for (SolutionOperationService so : requestOperations) {
                hasTransformed = true;
                so.execute(context);
            }
        } else if (context.getCurrentDirection().equals(DataFlowDirection.RESPONSE)) {
            for (SolutionOperationService so : responseOperations) {
                hasTransformed = true;
                so.execute(context);
            }
        }
    }

    @Override
    public String getName() {
        return configuration.getSolutionName();
    }

    @Override
    public String getId() {
        return configuration.getId().toString();
    }

    @Override
    public boolean hasTransformed() {
        return hasTransformed;
    }
}
