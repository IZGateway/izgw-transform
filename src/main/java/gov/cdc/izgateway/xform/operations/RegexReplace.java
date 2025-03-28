package gov.cdc.izgateway.xform.operations;

import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.enums.DataType;
import gov.cdc.izgateway.xform.exceptions.OperationException;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegexReplace extends BaseOperation implements Operation {

    @NotBlank(message = "required and cannot be empty")
    private String field;
    @NotBlank(message = "required and cannot be empty")
    private String regex;
    @NotBlank(message = "required and cannot be empty")
    private String replacement;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegexReplace.class);

    public RegexReplace() {}

    protected RegexReplace(RegexReplace regexReplace) {
        super(regexReplace.getId(), regexReplace.getOrder());
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
