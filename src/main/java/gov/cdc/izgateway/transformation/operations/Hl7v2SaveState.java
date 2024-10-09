package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.exceptions.OperationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Hl7v2SaveState extends SaveState implements Operation {
    public Hl7v2SaveState(SaveState saveState) {
        super(saveState);
    }

    @Override
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
