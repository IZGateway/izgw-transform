package gov.cdc.izgateway.transformation.camel.producers.hub;

import ca.uhn.hl7v2.HL7Exception;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
public class IZGHubProducer  extends DefaultProducer {
    @Autowired
    private HubMessageSender messageSender;

    public IZGHubProducer(Endpoint endpoint) {
        super(endpoint);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
//        log.info("IZGHubProducer");
//        HubWsdlTransformationContext context = exchange.getIn().getBody(HubWsdlTransformationContext.class);
//        try {
//            context.getSubmitSingleMessageRequest().setHl7Message(context.getServiceContext().getRequestMessage().encode());
//        }
//        catch (HL7Exception e) {
//            throw new HubControllerFault(e.getMessage());
//        }
//        // TODO: Paul - discussed with Keith and this destination will be a fixed thing - not a destination IIS... think about this more.
//        IDestination dest = getDestination("0");
//        // logDestination(dest);
//
//        // checkMessage(submitSingleMessage);
//
//        SubmitSingleMessageResponse response = messageSender.sendSubmitSingleMessage(dest, context.getSubmitSingleMessageRequest());
//
//        // Camel start for handling response transformation
//        context.getServiceContext().setCurrentDirection(DataFlowDirection.RESPONSE);
//        try {
//            context.getServiceContext().setResponseMessage(parseHl7v2Message(response.getHl7Message()));
//            producerTemplate.sendBody("direct:izghubTransform", context);
//            response.setHl7Message(serviceContext.getResponseMessage().encode());
//        }
//        catch (HL7Exception e) {
//            throw new HubControllerFault(e.getMessage());
//        }

//        System.out.println("IZGHubProducer");
//        System.out.println("Endpoint: " + getEndpoint().getEndpointUri());
//        // Your custom logic here
//        String body = exchange.getIn().getBody(String.class);
//        WebClient webClient = WebClient.create();
//        String response = webClient.put()
//                .uri("http://localhost:8081/izghubstub")
//                .body(BodyInserters.fromValue(body))
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//
//        System.out.println("Response: " + response);

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
