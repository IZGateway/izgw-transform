package gov.cdc.izgateway.transformation.preconditions;

import gov.cdc.izgateway.soap.message.SubmitSingleMessageRequest;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageResponse;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.context.XformContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotEquals extends Equals implements Precondition {

    protected NotEquals() {
        super();
    }

    protected NotEquals(NotEquals notEquals) {
        super(notEquals);
    }

    protected NotEquals(String dataPath, String comparisonValue) {
        super(dataPath, comparisonValue);
    }

    @Override
    public boolean evaluate(ServiceContext context) {
        return false;
    }

    @Override
    public boolean evaluate(XformContext<SubmitSingleMessageRequest, SubmitSingleMessageResponse> context) { return false; }
}
