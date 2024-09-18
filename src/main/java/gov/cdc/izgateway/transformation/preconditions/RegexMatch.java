package gov.cdc.izgateway.transformation.preconditions;

import gov.cdc.izgateway.transformation.annotations.ExcludeField;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Getter
@Setter
public class RegexMatch implements Precondition {
    private UUID id;
    private String dataPath;
    private String regex;

    @ExcludeField
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegexMatch.class);

    protected RegexMatch() {
    }

    protected RegexMatch(RegexMatch regexMatch) {
        this.id = regexMatch.id;
        this.dataPath = regexMatch.dataPath;
        this.regex = regexMatch.regex;
    }

    protected RegexMatch(UUID id, String dataPath, String regex) {
        this.id = id;
        this.dataPath = dataPath;
        this.regex = regex;
    }

    @Override
    public boolean evaluate(ServiceContext context) {

        if (log.isTraceEnabled()) {
            log.trace("Precondition: {} / id: '{}' / dataPath: '{}' / regex: '{}'",
                    this.getClass().getSimpleName(),
                    this.getId().toString(),
                    this.getDataPath(),
                    this.getRegex());
        }

        if (this.dataPath.startsWith("state.")) {
            String stateKey = this.dataPath.split("\\.")[1];
            String stateValue = context.getState().get(stateKey);
            Matcher matcher = getMatcher(stateValue);
            return matcher.matches();
        } else if (context.getDataType().equals(DataType.HL7V2)) {
            return new Hl7v2RegexMatch(this).evaluate(context);
        }

        return false;
    }

    protected Matcher getMatcher(String sourceValue) {
        Pattern pattern = Pattern.compile(this.getRegex());
        return pattern.matcher(sourceValue);
    }
}
