package gov.cdc.izgateway.xform.operations;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import gov.cdc.izgateway.xform.annotations.CaptureXformAdvice;
import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.exceptions.OperationException;
import lombok.extern.slf4j.Slf4j;

/**
 * HL7 v2 implementation of the {@link SaveState} operation.
 */
@Slf4j
public class Hl7v2SaveState extends SaveState implements Operation {
    /**
     * Creates an HL7 v2 save state operation with the settings of another operation.
     * @param saveState the operation to copy the settings from
     */
    public Hl7v2SaveState(SaveState saveState) {
        super(saveState);
    }

    @Override
    @CaptureXformAdvice
    public void execute(ServiceContext context) throws OperationException {

        try {
            Message message = context.getCurrentMessage();

            Terser terser = new Terser(message);

            String sourceValue = terser.get(this.getField());
            context.getState().put(this.getKey(), sourceValue);
        } catch (HL7Exception ex) {
            throw new OperationException(ex.getMessage(), ex.getCause());
        }
    }
}
