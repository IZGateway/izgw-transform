package gov.cdc.izgateway.transformation.camel.producers.hub;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.model.IDestination;
import gov.cdc.izgateway.soap.fault.Fault;
import gov.cdc.izgateway.soap.fault.UnexpectedExceptionFault;
import gov.cdc.izgateway.soap.message.FaultMessage;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageResponse;
import gov.cdc.izgateway.transformation.context.HubWsdlTransformationContext;
import gov.cdc.izgateway.transformation.endpoints.hub.HubControllerFault;
import gov.cdc.izgateway.transformation.endpoints.hub.forreview.HubMessageSender;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.util.Hl7Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;

@Slf4j
public class IZGHubProducer extends DefaultProducer {

    public IZGHubProducer(Endpoint endpoint) {
        super(endpoint);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        IZGHubComponent hubComponent = (IZGHubComponent) getEndpoint().getComponent();
        HubMessageSender messageSender = hubComponent.getMessageSender();

        HubWsdlTransformationContext context = exchange.getIn().getBody(HubWsdlTransformationContext.class);
        try {
            context.getSubmitSingleMessageRequest().setHl7Message(context.getServiceContext().getRequestMessage().encode());
        }
        catch (HL7Exception e) {
            throw new HubControllerFault(e.getMessage());
        }

        // TODO: Paul - discussed with Keith and this destination will be a fixed thing - not a destination IIS... think about this more.
        IDestination dest = hubComponent.getDestination("0");

        SubmitSingleMessageResponse response = null;
        try {
	        response = messageSender.sendSubmitSingleMessage(dest, context.getSubmitSingleMessageRequest());
	        context.getServiceContext().setCurrentDirection(DataFlowDirection.RESPONSE);
	        context.getServiceContext().setResponseMessage(Hl7Utils.parseHl7v2Message(response.getHl7Message()));
	        context.setSubmitSingleMessageResponse(response);
        } catch (Fault f) {
        	context.setFaultMessage(new FaultMessage(f, FaultMessage.HUB_NS));
        } catch (Exception hex) {
        	UnexpectedExceptionFault uex = new UnexpectedExceptionFault(hex, hex.getMessage());
        	FaultMessage fm = new FaultMessage(uex, FaultMessage.HUB_NS);
        	fm.getHubHeader().setDestinationUri(response == null ? null : response.getHubHeader().getDestinationUri());
        	context.setFaultMessage(fm);
        }

    }

}
