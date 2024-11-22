package gov.cdc.izgateway.xform.solutions;

import gov.cdc.izgateway.xform.annotations.CaptureXformAdvice;
import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.enums.DataFlowDirection;
import gov.cdc.izgateway.xform.exceptions.SolutionException;
import gov.cdc.izgateway.xform.exceptions.SolutionOperationException;
import gov.cdc.izgateway.xform.logging.advice.Advisable;
import gov.cdc.izgateway.xform.logging.advice.Transformable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Solution implements Advisable, Transformable {

    private final gov.cdc.izgateway.xform.model.Solution configuration;
    private final List<SolutionOperation> requestOperations;
    private final List<SolutionOperation> responseOperations;
    private boolean hasTransformed = false;

    public Solution(gov.cdc.izgateway.xform.model.Solution configuration) {
        this.configuration = configuration;
        requestOperations = new ArrayList<>();
        responseOperations = new ArrayList<>();

        for (gov.cdc.izgateway.xform.model.SolutionOperation so : configuration.getRequestOperations()) {
            requestOperations.add(new SolutionOperation(so));
        }

        for (gov.cdc.izgateway.xform.model.SolutionOperation so : configuration.getResponseOperations()) {
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
                    throw new SolutionException(String.format("Failed to execute solution: %s - %s", so.getClass().getSimpleName(), e.getMessage()), e.getCause());
                }
            }
        } else if (context.getCurrentDirection().equals(DataFlowDirection.RESPONSE)) {
            for (SolutionOperation so : responseOperations) {
                hasTransformed = true;
                try {
                    so.execute(context);
                } catch (SolutionOperationException e) {
                    throw new SolutionException(String.format("Failed to execute solution: %s - %s", so.getClass().getSimpleName(), e.getMessage()), e.getCause());
                }
            }
        }

    }

    @Override
    public String getName() {
        return configuration.getSolutionName();
    }

    @Override
    public UUID getId() {
        return configuration.getId();
    }

    @Override
    public boolean hasTransformed() {
        return hasTransformed;
    }
}
