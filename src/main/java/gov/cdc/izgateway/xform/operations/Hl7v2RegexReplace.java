package gov.cdc.izgateway.xform.operations;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import gov.cdc.izgateway.xform.annotations.CaptureXformAdvice;
import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.exceptions.OperationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Hl7v2RegexReplace extends RegexReplace implements Operation {

    public Hl7v2RegexReplace(RegexReplace regexReplace) {
        super(regexReplace);
    }

    @Override
    @CaptureXformAdvice
    public void execute(ServiceContext context) throws OperationException {

        try {
            Message message = context.getCurrentMessage();
            Terser terser = new Terser(message);
            String sourceValue = getField(terser);
            if (sourceValue != null) {
                String cleanedValue = sourceValue.replaceAll(this.getRegex(), this.getReplacement());
                terser.set(this.getField(), cleanedValue);
                context.setCurrentMessage(message);
            }
        } catch (HL7Exception ex) {
            throw new OperationException(ex.getMessage(), ex.getCause());
        }
    }
    
    private String getField(Terser terser) {
        try {
			return terser.get(this.getField());
		} catch (HL7Exception e) {
			// The field does not exist, simply return null.
			return null;
		}
    }
}

