package gov.cdc.izgateway.transformation.pipes;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.configuration.*;
import gov.cdc.izgateway.transformation.operations.ConditionalOperation;
import gov.cdc.izgateway.transformation.solutions.Solution;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Log
@Setter
public class Hl7v2Pipe extends BasePipe implements Pipe {

    @Setter(AccessLevel.NONE)
    private List<ConditionalOperation> preconditions;
    private Solution solution;

    public Hl7v2Pipe(PipeConfig configuration) {
        super(configuration);
        preconditions = new ArrayList<>();
    }

    @Override
    public void executeThisPipe(Message message) throws HL7Exception {
        // TODO - determine if we are working w/ request or response
        if (preconditionPass(message)) {
            log.log(Level.WARNING, "Precondition Passed");
            solution.executeRequest(message);
            solution.executeResponse(message);
        } else {
            log.log(Level.WARNING, "Precondition Failed");
        }
    }

    // TODO - add to interface/base
    public void addPrecondition(ConditionalOperation op) {
        preconditions.add(op);
    }

    private boolean preconditionPass(Message message) throws HL7Exception {
        boolean pass = true;

        for (ConditionalOperation op : preconditions) {
            pass = pass && op.evaluate(message);
        }

        return pass;
    }
}
