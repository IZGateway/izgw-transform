package gov.cdc.izgateway.transformation.pipes;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.operations.ConditionalOperation;
import gov.cdc.izgateway.transformation.preconditions.Precondition;
import gov.cdc.izgateway.transformation.solutions.Solution;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Setter
@Log
abstract class BasePipe implements Pipe {
    @Setter(AccessLevel.NONE)
    private Pipe next;

    @Setter(AccessLevel.NONE)
    protected boolean preconditionChecked;

    @Setter(AccessLevel.NONE)
    protected boolean preconditionPassed;

    protected Solution solution;
    protected List<Precondition> preconditions;

    protected BasePipe() {
        preconditions = new ArrayList<>();
        preconditionChecked = false;
        preconditionPassed = false;
    }

    @Override
    public void execute(ServiceContext context) throws HL7Exception {
        executeCondition(context);

        if (preconditionPassed) {
            solution.execute(context);
        }

        executeNextPipe(context);
    }

    @Override
    public void executeCondition(ServiceContext context) throws HL7Exception {
        // TODO - change to protected?
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
    }

    private boolean preconditionPass(Message message) throws HL7Exception {
        boolean pass = true;

        for (Precondition op : preconditions) {
            pass = pass && op.evaluate(message);
        }

        return pass;
    }

    @Override
    public void setNextPipe(Pipe pipe) {
        this.next = pipe;
    }

    @Override
    public Pipe getNextPipe() {
        return next;
    }

    @Override
    public void addPrecondition(Precondition op) {
        preconditions.add(op);
    }

    private void executeNextPipe(ServiceContext context) throws HL7Exception {
        if (next != null) {
            next.execute(context);
        }
    }
}
