package gov.cdc.izgateway.transformation.pipes;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.configuration.*;
import gov.cdc.izgateway.transformation.context.ServiceContext;
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

    // TODO - Solution level in the pipeline do we give the end user ability to
    //        set precondition on Request/Response?  Or just request.  Or at all?
    @Setter(AccessLevel.NONE)
    private boolean preconditionChecked;
    @Setter(AccessLevel.NONE)
    private boolean preconditionPassed;

    public Hl7v2Pipe(PipeConfig configuration) {
        super(configuration);
        preconditions = new ArrayList<>();
        preconditionChecked = false;
        preconditionPassed = false;
    }

    @Override
    public void executeThisPipe(ServiceContext context) throws HL7Exception {

        if (!preconditionChecked) {
            preconditionPassed = preconditionPass(context.getRequestMessage());

            // TODO - remove or make only log on DEBUG
            if (preconditionPassed) {
                log.log(Level.WARNING, "Precondition Passed");
            } else {
                log.log(Level.WARNING, "Precondition Failed");
            }

            preconditionChecked = true;
        }

        if (preconditionPassed) {
            solution.execute(context);
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
