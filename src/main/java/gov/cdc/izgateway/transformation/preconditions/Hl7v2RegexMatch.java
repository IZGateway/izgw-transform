package gov.cdc.izgateway.transformation.preconditions;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import gov.cdc.izgateway.transformation.context.ServiceContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hl7v2RegexMatch extends RegexMatch implements Precondition {

    public Hl7v2RegexMatch() {}

    public Hl7v2RegexMatch(String dataPath, String regex) {
        super(dataPath, regex);
    }

    public Hl7v2RegexMatch(RegexMatch regexMatch) {
        super(regexMatch);
    }

    @Override
    public boolean evaluate(ServiceContext context) {

        Message message = context.getCurrentMessage();

        Terser terser = new Terser(message);

        String sourceValue;
        try {
            sourceValue = terser.get(this.getDataPath());
        } catch (HL7Exception e) {
            // HL7Exception will happen if Terser.get references Segment that does not exist in the message
            // In this case just returning false.  Entirely possible that Analyst/Engineer has a precondition
            // set for a segment that just simply won't exist in every message submitted.
            return false;
        }

        if (sourceValue == null) {
            return false;
        }

        Pattern pattern = Pattern.compile(this.getRegex());
        Matcher matcher = pattern.matcher(sourceValue);
        return matcher.matches();
    }

}
