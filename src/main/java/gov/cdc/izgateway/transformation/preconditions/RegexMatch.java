package gov.cdc.izgateway.transformation.preconditions;

import ca.uhn.hl7v2.model.Message;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegexMatch implements Precondition {
    private String dataPath;
    private String regex;

    protected RegexMatch() {}

    protected RegexMatch(RegexMatch regexMatch) {
        this.dataPath = regexMatch.dataPath;
        this.regex = regexMatch.regex;
    }

    protected RegexMatch(String dataPath, String regex) {
        this.dataPath = dataPath;
        this.regex = regex;
    }

    @Override
    public boolean evaluate(Message message) {
        return false;
    }
}
