package gov.cdc.izgateway.transformation.preconditions;

import gov.cdc.izgateway.soap.message.SubmitSingleMessageRequest;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageResponse;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.context.XformContext;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RegexMatch implements Precondition {
    private UUID id;
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
    public boolean evaluate(ServiceContext context) {
        return false;
    }

    @Override
    public boolean evaluate(XformContext<SubmitSingleMessageRequest, SubmitSingleMessageResponse> context) {
        return false;
    }
}
