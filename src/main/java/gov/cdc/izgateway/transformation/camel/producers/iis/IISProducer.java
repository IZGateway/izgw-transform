package gov.cdc.izgateway.transformation.camel.producers.iis;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.logging.info.DestinationInfo;
import gov.cdc.izgateway.model.IDestination;
import gov.cdc.izgateway.model.IDestinationId;
import gov.cdc.izgateway.soap.fault.UnknownDestinationFault;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageResponse;
import gov.cdc.izgateway.transformation.camel.producers.ProducerBase;
import gov.cdc.izgateway.transformation.context.IZGTransformationContext;
import gov.cdc.izgateway.transformation.endpoints.hub.HubControllerFault;
import gov.cdc.izgateway.transformation.endpoints.hub.HubMessageSender;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.model.Destination;
import gov.cdc.izgateway.transformation.model.DestinationId;
import gov.cdc.izgateway.transformation.util.Hl7Utils;
import gov.cdc.izgateway.utils.SystemUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;

@Slf4j
public class IISProducer extends ProducerBase {

    public IISProducer(Endpoint endpoint) {
        super(endpoint);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        HubMessageSender messageSender = getIISComponent().getMessageSender();

        IZGTransformationContext context = exchange.getIn().getBody(IZGTransformationContext.class);

        IDestination destination = createDestination(getIISComponent());
        destination.setUsername(context.getSubmitSingleMessageRequest().getUsername());
        destination.setPassword(context.getSubmitSingleMessageRequest().getPassword());
        setDestinationInfoFromDestination(RequestContext.getDestinationInfo(), destination);

        try {
            context.getSubmitSingleMessageRequest().setHl7Message(context.getServiceContext().getRequestMessage().encode());
        }
        catch (HL7Exception e) {
            throw new HubControllerFault(e.getMessage());
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
