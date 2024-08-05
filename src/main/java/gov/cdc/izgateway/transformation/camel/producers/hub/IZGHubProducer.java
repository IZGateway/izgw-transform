package gov.cdc.izgateway.transformation.camel.producers.hub;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.model.IDestination;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageResponse;
import gov.cdc.izgateway.transformation.context.HubWsdlTransformationContext;
import gov.cdc.izgateway.transformation.endpoints.hub.HubControllerFault;
import gov.cdc.izgateway.transformation.endpoints.hub.HubMessageSender;
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

        SubmitSingleMessageResponse response = messageSender.sendSubmitSingleMessage(dest, context.getSubmitSingleMessageRequest());
        context.getServiceContext().setCurrentDirection(DataFlowDirection.RESPONSE);
        context.getServiceContext().setResponseMessage(Hl7Utils.parseHl7v2Message(response.getHl7Message()));
        context.setSubmitSingleMessageResponse(response);

    }

}
