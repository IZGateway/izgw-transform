package gov.cdc.izgateway.transformation.preconditions;

import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Getter
@Setter
@Slf4j
public class RegexMatch implements Precondition {
    private UUID id;
    private String dataPath;
    private String regex;

    protected RegexMatch() {}

    protected RegexMatch(RegexMatch regexMatch) {
        this.id = regexMatch.id;
        this.dataPath = regexMatch.dataPath;
        this.regex = regexMatch.regex;
    }

    protected RegexMatch(String dataPath, String regex) {
        this.dataPath = dataPath;
        this.regex = regex;
    }

    @Override
    public boolean evaluate(ServiceContext context) {

        log.trace(String.format("Precondition: %s / id: '%s' / dataPath: '%s' / regex: '%s'",
                this.getClass().getSimpleName(),
                this.getId().toString(),
                this.getDataPath(),
                this.getRegex()));

        if (context.getDataType().equals(DataType.HL7V2)) {
            return new Hl7v2RegexMatch(this).evaluate(context);
        }

        return false;
    }
}
