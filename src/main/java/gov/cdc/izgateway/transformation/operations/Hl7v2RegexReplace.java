package gov.cdc.izgateway.transformation.operations;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.exceptions.OperationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Hl7v2RegexReplace extends RegexReplace implements Operation {

    public Hl7v2RegexReplace(RegexReplace regexReplace) {
        super(regexReplace);
    }

    @Override
    public void execute(ServiceContext context) throws OperationException {

        try {
            Message message = context.getCurrentMessage();

            Terser terser = new Terser(message);

            String sourceValue = terser.get(this.getField());
            if (sourceValue != null) {
                String cleanedValue = sourceValue.replaceAll(this.getRegex(), this.getReplacement());
                terser.set(this.getField(), cleanedValue);

                context.setCurrentMessage(message);
            }
        } catch (HL7Exception ex) {
            throw new OperationException(ex.getMessage(), ex.getCause());
        }
        
    }
}

