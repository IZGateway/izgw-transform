package gov.cdc.izgateway.transformation.solutions;

// To repalce Solution as we refactor to pull from different files.

import gov.cdc.izgateway.transformation.configuration.SolutionOperationsConfig;
import gov.cdc.izgateway.transformation.model.Solution;
import gov.cdc.izgateway.transformation.model.SolutionOperation;

import java.util.ArrayList;
import java.util.List;

public class SolutionService {

    private final Solution configuration;
    private final List<SolutionOperation> requestOperations;

    public SolutionService(Solution configuration) {
        this.configuration = configuration;
        requestOperations = new ArrayList<>();

        for (SolutionOperation so : configuration.getRequestOperations()) {
            // TODO going to have to build a new SolutionOperation object for refactor.
        }

    }
}
