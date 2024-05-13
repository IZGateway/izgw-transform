package gov.cdc.izgateway.transformation.solutions;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.configuration.SolutionConfig;
import lombok.extern.java.Log;

@Log
public class Solution {
    private final SolutionOperations requestOperations;
    private final SolutionOperations responseOperations;

    public Solution(SolutionConfig configuration) {
        requestOperations = new SolutionOperations(configuration.getRequestOperations());
        responseOperations = new SolutionOperations(configuration.getResponseOperations());
    }

    // TODO - make generic not HL7 specfic
    // TODO - pass state or context around and determine from that to run request or response
    public void executeRequest(Message message) throws HL7Exception {
        requestOperations.execute(message);
    }

    // TODO - make generic not HL7 specfic
    // TODO - pass state or context around and determine from that to run request or response
    public void executeResponse(Message message) throws HL7Exception {
        responseOperations.execute(message);
    }
}
