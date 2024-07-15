package gov.cdc.izgateway.transformation.solutions;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.transformation.annotations.CaptureXformAdvice;
import gov.cdc.izgateway.transformation.configuration.SolutionConfig;
import gov.cdc.izgateway.transformation.configuration.SolutionOperationsConfig;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.enums.DataType;
import gov.cdc.izgateway.transformation.logging.advice.Advisable;
import lombok.Data;
import lombok.Getter;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;

@Log
@Getter
public class Solution implements Advisable {

    // TODO - Create SolutionOperationChain instead of list?
    private final List<SolutionOperation> requestOperations;
    private final List<SolutionOperation> responseOperations;
    private SolutionConfig configuration;
    private boolean hasTransformed = false;

    public Solution(SolutionConfig configuration, DataType dataType) {
        this.configuration = configuration;
        requestOperations = new ArrayList<>();
        for (SolutionOperationsConfig soc : configuration.getRequestOperations()) {
            requestOperations.add(new SolutionOperation(soc, dataType));
        }

        responseOperations = new ArrayList<>();
        for (SolutionOperationsConfig soc : configuration.getResponseOperations()) {
            responseOperations.add(new SolutionOperation(soc, dataType));
        }

    }

    // TODO - make generic not HL7 specific
    @CaptureXformAdvice
    public void execute(ServiceContext context) throws HL7Exception {
        if (context.getCurrentDirection().equals(DataFlowDirection.REQUEST)) {

            for (SolutionOperation op : requestOperations) {
                hasTransformed = true;
                op.execute(context);
            }

        } else if (context.getCurrentDirection().equals(DataFlowDirection.RESPONSE)) {

            for (SolutionOperation op : responseOperations) {
                hasTransformed = true;
                op.execute(context);
            }

        }
    }

    @Override
    public String getName() {
        return configuration.getName();
    }

    @Override
    public String getId() {
        return configuration.getId().toString();
    }

    @Override
    public boolean hasTransformed() {
        return hasTransformed;
    }

    @Override
    public boolean preconditionPassed() {
        return true;
    }
}
