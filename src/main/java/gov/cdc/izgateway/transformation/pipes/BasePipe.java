package gov.cdc.izgateway.transformation.pipes;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.configuration.PipeConfig;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataType;
import gov.cdc.izgateway.transformation.preconditions.*;
import gov.cdc.izgateway.transformation.solutions.Solution;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


@Setter
@Slf4j
abstract class BasePipe implements Pipe {
    @Setter(AccessLevel.NONE)
    private Pipe next;

    @Setter(AccessLevel.NONE)
    protected boolean preconditionChecked;

    @Setter(AccessLevel.NONE)
    protected boolean preconditionPassed;

    protected PipeConfig config;
    protected Solution solution;
    protected List<Precondition> preconditions;

    protected BasePipe() {
        preconditions = new ArrayList<>();
        preconditionChecked = false;
        preconditionPassed = false;
    }

    protected BasePipe(PipeConfig config, ServiceContext context) {
        this();
        this.config = config;

        for (Precondition precondition : config.getPreconditions()) {
            if (context.getDataType().equals(DataType.HL7V2) && precondition.getClass().equals(Equals.class)) {
                this.addPrecondition(new Hl7v2Equals((Equals) precondition));
            } else if (context.getDataType().equals(DataType.HL7V2) && precondition.getClass().equals(NotEquals.class)) {
                this.addPrecondition(new Hl7v2NotEquals((NotEquals) precondition));
            } else if (context.getDataType().equals(DataType.HL7V2) && precondition.getClass().equals(Exists.class)) {
                this.addPrecondition(new Hl7v2Exists((Exists) precondition));
            } else if (context.getDataType().equals(DataType.HL7V2) && precondition.getClass().equals(NotExists.class)) {
                this.addPrecondition(new Hl7v2NotExists((NotExists) precondition));
            } else if (context.getDataType().equals(DataType.HL7V2) && precondition.getClass().equals(RegexMatch.class)) {
                this.addPrecondition(new Hl7v2RegexMatch((RegexMatch) precondition));
            }
        }

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
        if (!preconditionChecked) {
            preconditionPassed = preconditionPass(context.getRequestMessage());

            if (preconditionPassed) {
                log.trace("Pipe Precondition Passed");
            } else {
                log.trace("Pipe Precondition Failed");
            }

            preconditionChecked = true;
        }
    }

    private boolean preconditionPass(Message message) {
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
