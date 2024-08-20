package gov.cdc.izgateway.transformation.preconditions;

import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageRequest;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageResponse;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.context.XformContext;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Exists implements Precondition {

    private UUID id;
    private String dataPath;

    protected Exists() {}

    protected Exists(Exists exists) {
        this.dataPath = exists.getDataPath();
    }

    protected Exists(String dataPath) {
        this.dataPath =  dataPath;
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
