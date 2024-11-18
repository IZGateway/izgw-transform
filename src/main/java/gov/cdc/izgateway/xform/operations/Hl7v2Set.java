package gov.cdc.izgateway.xform.operations;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import gov.cdc.izgateway.xform.annotations.CaptureXformAdvice;
import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.exceptions.OperationException;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Hl7v2Set extends Set implements Operation {

    public Hl7v2Set(Set set) {
        super(set);
    }

    @Override
    @CaptureXformAdvice
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
