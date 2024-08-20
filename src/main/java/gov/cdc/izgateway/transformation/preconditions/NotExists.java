package gov.cdc.izgateway.transformation.preconditions;

import gov.cdc.izgateway.soap.message.SubmitSingleMessageRequest;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageResponse;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.context.XformContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotExists extends Exists implements Precondition {
    protected NotExists() {}

    protected NotExists(NotExists notExists) {
        super(notExists);
    }

    protected NotExists(String dataPath) {
        super(dataPath);
    }

    @Override
    public boolean evaluate(ServiceContext context) {
        return false;
    }

    @Override
    public boolean evaluate(XformContext<SubmitSingleMessageRequest, SubmitSingleMessageResponse> context) { return false; }
}
