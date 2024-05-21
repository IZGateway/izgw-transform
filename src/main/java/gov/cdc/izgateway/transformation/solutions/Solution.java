package gov.cdc.izgateway.transformation.solutions;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.transformation.configuration.ServiceConfig;
import gov.cdc.izgateway.transformation.configuration.SolutionConfig;
import gov.cdc.izgateway.transformation.configuration.SolutionOperationsConfig;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Log
public class Solution {

    // TODO - Create SolutionOperationChain instead of list?
    private final List<SolutionOperation> requestOperations;
    private final List<SolutionOperation> responseOperations;

    public Solution(SolutionConfig configuration) {
        requestOperations = new ArrayList<>();
        for (SolutionOperationsConfig soc : configuration.getRequestOperations()) {
            requestOperations.add(new SolutionOperation(soc));
        }

        responseOperations = new ArrayList<>();
        for (SolutionOperationsConfig soc : configuration.getResponseOperations()) {
            responseOperations.add(new SolutionOperation(soc));
        }

    }

    public Solution(ServiceContext context) {
        requestOperations = new ArrayList<>();
        responseOperations = new ArrayList<>();
    }

    // TODO - make generic not HL7 specific
    public void execute(ServiceContext context) throws HL7Exception {
        if (context.getCurrentDirection().equals(DataFlowDirection.REQUEST)) {

            for (SolutionOperation op : requestOperations) {
                op.execute(context.getRequestMessage());
            }

        } else if (context.getCurrentDirection().equals(DataFlowDirection.RESPONSE)) {

            for (SolutionOperation op : responseOperations) {
                op.execute(context.getResponseMessage());
            }

        }
    }
}
