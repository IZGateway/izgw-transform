package gov.cdc.izgateway.xform.preconditions;

import gov.cdc.izgateway.xform.annotations.CaptureXformAdvice;
import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.enums.DataType;
import gov.cdc.izgateway.xform.logging.advice.Advisable;
import gov.cdc.izgateway.xform.logging.advice.Transformable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;


@Getter
@Setter
public class RegexMatch implements Precondition, Advisable, Transformable {
    @NotNull(message = "required and cannot be empty")
    private UUID id;
    @NotNull(message = "required and cannot be empty")
    private String dataPath;
    @NotNull(message = "required and cannot be empty")
    private String regex;

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
    @CaptureXformAdvice
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
            Matcher matcher = getMatcher(StringUtils.defaultString(stateValue));
            return matcher.matches();
        } else if (context.getDataType().equals(DataType.HL7V2)) {
            return new Hl7v2RegexMatch(this).evaluate(context);
        }

        return false;
    }

    protected Matcher getMatcher(String sourceValue) {
        Pattern pattern = Pattern.compile(this.getRegex());
        return pattern.matcher(StringUtils.defaultString(sourceValue));
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean hasTransformed() {
        return true;
    }
}
