package gov.cdc.izgateway.xform.camel.producers.iis;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.model.IDestination;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageResponse;
import gov.cdc.izgateway.xform.camel.producers.ProducerBase;
import gov.cdc.izgateway.xform.context.IZGXformContext;
import gov.cdc.izgateway.xform.endpoints.hub.HubControllerFault;
import gov.cdc.izgateway.xform.endpoints.hub.HubMessageSender;
import gov.cdc.izgateway.xform.enums.DataFlowDirection;
import gov.cdc.izgateway.xform.util.Hl7Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;

@Slf4j
public class IISProducer extends ProducerBase {

    public IISProducer(Endpoint endpoint) {
        super(endpoint);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        HubMessageSender messageSender = getIISComponent().getMessageSender();

        IZGXformContext context = exchange.getIn().getBody(IZGXformContext.class);

        IDestination destination = createDestination(getIISComponent());
        destination.setUsername(context.getSubmitSingleMessageRequest().getUsername());
        destination.setPassword(context.getSubmitSingleMessageRequest().getPassword());
        setDestinationInfoFromDestination(RequestContext.getDestinationInfo(), destination);

        try {
            context.getSubmitSingleMessageRequest().setHl7Message(context.getServiceContext().getRequestMessage().encode());
        }
        catch (HL7Exception e) {
            throw new HubControllerFault(e);
        }

        SubmitSingleMessageResponse response = messageSender.sendSubmitSingleMessage(destination,
                context.getSubmitSingleMessageRequest());
        context.getServiceContext().setCurrentDirection(DataFlowDirection.RESPONSE);
        context.getServiceContext().setResponseMessage(Hl7Utils.parseHl7v2Message(response.getHl7Message()));
        context.setSubmitSingleMessageResponse(response);

    }

    private IISComponent getIISComponent() {
        return (IISComponent) getEndpoint().getComponent();
    }

}
