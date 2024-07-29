package gov.cdc.izgateway.transformation.context;

import gov.cdc.izgateway.soap.message.FaultMessage;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageRequest;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * Context object required for a pipeline that uses the Hub outbound endpoint.
 * 
 * @author Audacious Inquiry
 */
@Getter
@Setter
public class HubWsdlTransformationContext {
    private ServiceContext serviceContext;
    private SubmitSingleMessageRequest submitSingleMessageRequest;
    private SubmitSingleMessageResponse submitSingleMessageResponse;
    private FaultMessage faultMessage;
    
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
    
    /**
     * Get the fault status.
     * @return true if the context reflects that a fault occured.
     */
    public boolean hasFault() {
    	return faultMessage != null;
    }
    
    /**
     * See if there is a response (if not, there should be a fault).
     * @return	true if there is a response.
     */
    public boolean hasResponse() {
    	return submitSingleMessageResponse != null;
    }
}