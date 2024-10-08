package gov.cdc.izgateway.transformation.solutions;

import gov.cdc.izgateway.transformation.annotations.CaptureXformAdvice;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.exceptions.SolutionException;
import gov.cdc.izgateway.transformation.exceptions.SolutionOperationException;
import gov.cdc.izgateway.transformation.logging.advice.Advisable;
import gov.cdc.izgateway.transformation.logging.advice.Transformable;

import java.util.ArrayList;
import java.util.List;

public class Solution implements Advisable, Transformable {

    private final gov.cdc.izgateway.transformation.model.Solution configuration;
    private final List<SolutionOperation> requestOperations;
    private final List<SolutionOperation> responseOperations;
    private boolean hasTransformed = false;

    public Solution(gov.cdc.izgateway.transformation.model.Solution configuration) {
        this.configuration = configuration;
        requestOperations = new ArrayList<>();
        responseOperations = new ArrayList<>();

        for (gov.cdc.izgateway.transformation.model.SolutionOperation so : configuration.getRequestOperations()) {
            requestOperations.add(new SolutionOperation(so));
        }

        for (gov.cdc.izgateway.transformation.model.SolutionOperation so : configuration.getResponseOperations()) {
            responseOperations.add(new SolutionOperation(so));
        }
    }

    @CaptureXformAdvice
    public void execute(ServiceContext context) throws SolutionException {

        if (context.getCurrentDirection().equals(DataFlowDirection.REQUEST)) {
            for (SolutionOperation so : requestOperations) {
                hasTransformed = true;
                try {
                    so.execute(context);
                } catch (SolutionOperationException e) {
                    throw new SolutionException("Failed to execute solution: " + so.getClass().getSimpleName(), e);
                }
            }
        } else if (context.getCurrentDirection().equals(DataFlowDirection.RESPONSE)) {
            for (SolutionOperation so : responseOperations) {
                hasTransformed = true;
                try {
                    so.execute(context);
                } catch (SolutionOperationException e) {
                    throw new SolutionException("Failed to execute solution: " + so.getClass().getSimpleName(), e);
                }
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
