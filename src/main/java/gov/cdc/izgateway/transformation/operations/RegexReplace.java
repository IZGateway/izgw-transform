package gov.cdc.izgateway.transformation.operations;

import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.enums.DataType;
import gov.cdc.izgateway.transformation.exceptions.OperationException;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RegexReplace implements Operation {

    private UUID id;
    private int order;
    private String field;
    private String regex;
    private String replacement;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegexReplace.class);

    public RegexReplace() {}

    protected RegexReplace(RegexReplace regexReplace) {
        this.id = regexReplace.id;
        this.order = regexReplace.order;
        this.field = regexReplace.field;
        this.regex = regexReplace.regex;
        this.replacement = regexReplace.replacement;
    }

    @Override
    public void execute(ServiceContext context) throws OperationException {

        if (log.isTraceEnabled()) {
            log.trace("Operation: {} on '{}' with regex '{}' and replacement '{}'", this.getClass().getSimpleName(), this.field, this.regex, this.replacement);
        }

        if (context.getDataType().equals(DataType.HL7V2)) {
            Hl7v2RegexReplace regexReplace = new Hl7v2RegexReplace(this);
            regexReplace.execute(context);
        }

    }
}
