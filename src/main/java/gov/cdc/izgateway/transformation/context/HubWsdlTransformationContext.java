package gov.cdc.izgateway.transformation.context;

import gov.cdc.izgateway.soap.message.SubmitSingleMessageRequest;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * Context object required for a pipeline that uses the Hub outbound endpoint.
 */
@Getter
@Setter
public class HubWsdlTransformationContext {
    private ServiceContext serviceContext;
    private SubmitSingleMessageRequest submitSingleMessageRequest;
    private SubmitSingleMessageResponse submitSingleMessageResponse;

    public HubWsdlTransformationContext(ServiceContext serviceContext, SubmitSingleMessageRequest submitSingleMessageRequest) {
        this.serviceContext = serviceContext;
        this.submitSingleMessageRequest = submitSingleMessageRequest;
        this.submitSingleMessageResponse = null;
    }

    public HubWsdlTransformationContext(ServiceContext serviceContext, SubmitSingleMessageRequest submitSingleMessageRequest, SubmitSingleMessageResponse submitSingleMessageResponse) {
        this.serviceContext = serviceContext;
        this.submitSingleMessageRequest = submitSingleMessageRequest;
        this.submitSingleMessageResponse = submitSingleMessageResponse;
    }

}