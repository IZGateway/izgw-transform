package gov.cdc.izgateway.transformation.solutions;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.transformation.configuration.SolutionConfig;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import lombok.extern.java.Log;

@Log
public class Solution {
    private final SolutionOperations requestOperations;
    private final SolutionOperations responseOperations;

    public Solution(SolutionConfig configuration) {
        requestOperations = new SolutionOperations(configuration.getRequestOperations());
        responseOperations = new SolutionOperations(configuration.getResponseOperations());
    }

    // TODO - make generic not HL7 specific
    public void execute(ServiceContext context) throws HL7Exception {
        if (context.getCurrentDirection().equals("REQUEST")) {
            requestOperations.execute(context.getRequestMessage());
        } else if (context.getCurrentDirection().equals("RESPONSE")) {
            responseOperations.execute(context.getResponseMessage());
        }
    }
}
