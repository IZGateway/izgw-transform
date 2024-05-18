package gov.cdc.izgateway.transformation.solutions;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.transformation.configuration.SolutionConfig;
import gov.cdc.izgateway.transformation.configuration.SolutionOperationsConfig;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import lombok.extern.java.Log;

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

    // TODO - make generic not HL7 specific
    public void execute(ServiceContext context) throws HL7Exception {
        if (context.getCurrentDirection().equals("REQUEST")) {

            for (SolutionOperation op : requestOperations) {
                op.execute(context.getRequestMessage());
            }

        } else if (context.getCurrentDirection().equals("RESPONSE")) {

            for (SolutionOperation op : responseOperations) {
                op.execute(context.getResponseMessage());
            }

        }
    }
}
