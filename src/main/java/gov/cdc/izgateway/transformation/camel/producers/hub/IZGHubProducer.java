package gov.cdc.izgateway.transformation.camel.producers.hub;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import gov.cdc.izgateway.model.IDestination;
import gov.cdc.izgateway.model.IDestinationId;
import gov.cdc.izgateway.soap.fault.UnknownDestinationFault;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageRequest;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageResponse;
import gov.cdc.izgateway.transformation.camel.HubWsdlTransformationContext;
import gov.cdc.izgateway.transformation.endpoints.hub.HubControllerFault;
import gov.cdc.izgateway.transformation.endpoints.hub.forreview.Destination;
import gov.cdc.izgateway.transformation.endpoints.hub.forreview.DestinationId;
import gov.cdc.izgateway.transformation.endpoints.hub.forreview.HubMessageSender;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.util.Hl7Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

@Slf4j
public class IZGHubProducer  extends DefaultProducer {

    public IZGHubProducer(Endpoint endpoint) {
        super(endpoint);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        log.info("IZGHubProducer");
        IZGHubComponent hubComponent = (IZGHubComponent) getEndpoint().getComponent();
        HubMessageSender messageSender = hubComponent.getMessageSender();
        log.info("IZGHubProducer 2");

        HubWsdlTransformationContext context = exchange.getIn().getBody(HubWsdlTransformationContext.class);
        try {
            context.getSubmitSingleMessageRequest().setHl7Message(context.getServiceContext().getRequestMessage().encode());
        }
        catch (HL7Exception e) {
            throw new HubControllerFault(e.getMessage());
        }
        // TODO: Paul - discussed with Keith and this destination will be a fixed thing - not a destination IIS... think about this more.
        IDestination dest = getDestination("0");

        SubmitSingleMessageResponse response = messageSender.sendSubmitSingleMessage(dest, context.getSubmitSingleMessageRequest());
        context.getServiceContext().setCurrentDirection(DataFlowDirection.RESPONSE);
        context.getServiceContext().setResponseMessage(Hl7Utils.parseHl7v2Message(response.getHl7Message()));
        context.setSubmitSingleMessageResponse(response);

    }

    private IDestination getDestination(String destinationId) throws UnknownDestinationFault {
        IDestinationId destinationIdObject = new DestinationId();
        destinationIdObject.setDestId(destinationId);
        destinationIdObject.setDestType(0);

        IDestination hubDestination = new Destination();
        hubDestination.setId(destinationIdObject);
        hubDestination.setDestUri("https://localhost/IISHubService");

        return hubDestination;
    }
}
