package gov.cdc.izgateway.xform.operations;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import gov.cdc.izgateway.xform.annotations.CaptureXformAdvice;
import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.exceptions.OperationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Hl7v2Copy extends Copy implements Operation {
    public Hl7v2Copy(Copy copy) {
        super(copy);
    }

    @Override
    @CaptureXformAdvice
    public void execute(ServiceContext context) throws OperationException {
        Message message = context.getCurrentMessage();

        Terser terser = new Terser(message);

        try {
            String sourceValue = terser.get(this.getSourceField());
            terser.set(this.getDestinationField(), sourceValue);
        } catch (HL7Exception hl7Exception) {
            log.error("Error while copying HL7v2 field: {} to {}", this.getSourceField(), this.getDestinationField(), hl7Exception);
            throw new OperationException(hl7Exception.getMessage(), hl7Exception.getCause());
        }

        context.setCurrentMessage(message);
    }
}
