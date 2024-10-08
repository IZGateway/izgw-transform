package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.exceptions.OperationException;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Hl7v2Set extends Set implements Operation {

    public Hl7v2Set(Set set) {
        super(set);
    }

    @Override
    public void execute(ServiceContext context) throws OperationException {

        try {
            Message message = context.getCurrentMessage();

            Terser terser = new Terser(message);
            terser.set(this.getDestinationField(), this.getSetValue());

            context.setCurrentMessage(message);
        } catch (HL7Exception e) {
            throw new OperationException(e.getMessage(), e.getCause());
        }
    }
}
