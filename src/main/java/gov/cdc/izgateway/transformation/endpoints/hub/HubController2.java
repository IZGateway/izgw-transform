package gov.cdc.izgateway.transformation.endpoints.hub;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.logging.event.TransactionData;
import gov.cdc.izgateway.model.IDestination;
import gov.cdc.izgateway.model.IDestinationId;
import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.security.Roles;
import gov.cdc.izgateway.service.IMessageHeaderService;
import gov.cdc.izgateway.soap.SoapControllerBase;
import gov.cdc.izgateway.soap.fault.Fault;
import gov.cdc.izgateway.soap.fault.SecurityFault;
import gov.cdc.izgateway.soap.fault.UnknownDestinationFault;
import gov.cdc.izgateway.soap.message.HasCredentials;
import gov.cdc.izgateway.soap.message.SoapMessage;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageRequest;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageResponse;
import gov.cdc.izgateway.soap.net.MessageSender;
import gov.cdc.izgateway.transformation.camel.HubWsdlTransformationContext;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.endpoints.hub.forreview.Destination;
import gov.cdc.izgateway.transformation.endpoints.hub.forreview.DestinationId;
import gov.cdc.izgateway.transformation.endpoints.hub.forreview.HubMessageSender;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.enums.DataType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

@RestController
@RolesAllowed({Roles.SOAP, Roles.ADMIN})
@RequestMapping("/IISHubService")
@Lazy(false)
@Slf4j
public class HubController2 extends SoapControllerBase {
    private MessageSender messageSender;
    private ProducerTemplate producerTemplate;

    @Autowired
    public HubController2(
            IMessageHeaderService mshService,
            HubMessageSender messageSender,
            AccessControlRegistry registry,
            ProducerTemplate producerTemplate
    ) {
        // The base schema for HUB messages is still the iis-2014 schema, with the exception of HubHeader and certain faults.
        super(mshService, SoapMessage.IIS2014_NS, "cdc-iis-hub.wsdl", Arrays.asList(SoapMessage.HUB_NS, SoapMessage.IIS2014_NS));
        this.messageSender = messageSender;
        this.producerTemplate = producerTemplate;

        registry.register(this);

        // TODO: Paul - this is temporary until I have a better understanding of this
        // Need to understand what we need to log and any other cross-cutting concerns
        // May be able to reuse the EventId class in core
        // We may want a new "thing" other TransactionData
        TransactionData t = new TransactionData("TODO: A Real EVENTID 1");
        RequestContext.setTransactionData(t);
    }

    @Override
    protected ResponseEntity<?> submitSingleMessage(SubmitSingleMessageRequest submitSingleMessage, String destinationId) throws Fault {
        // TODO: This is a placeholder for the real EVENTID
        TransactionData t = new TransactionData("TODO: A Real EVENTID");
        RequestContext.setTransactionData(t);

        // Start of Camel Routes for transformations
        // TODO Implement the organization properly
        UUID organization = UUID.fromString("0d15449b-fb08-4013-8985-20c148b353fe");
        ServiceContext serviceContext = getServiceContext(organization, submitSingleMessage.getHl7Message());
        serviceContext.setCurrentDirection(DataFlowDirection.REQUEST);

        HubWsdlTransformationContext context = new HubWsdlTransformationContext(serviceContext, submitSingleMessage, null);

        producerTemplate.sendBody("direct:izghubTransform", context);

        // Start of where I think the Camel components take over
        // End of Camel
        try {
            submitSingleMessage.setHl7Message(serviceContext.getRequestMessage().encode());
        }
        catch (HL7Exception e) {
            throw new HubControllerFault(e.getMessage());
        }

        // TODO: Paul - discussed with Keith and this destination will be a fixed thing - not a destination IIS... think about this more.
        IDestination dest = getDestination(destinationId);
        logDestination(dest);

        checkMessage(submitSingleMessage);

        SubmitSingleMessageResponse response = messageSender.sendSubmitSingleMessage(dest, submitSingleMessage);

        // Camel start for handling response transformation
        serviceContext.setCurrentDirection(DataFlowDirection.RESPONSE);
        try {
            serviceContext.setResponseMessage(parseHl7v2Message(response.getHl7Message()));
            producerTemplate.sendBody("direct:izghubTransform", context);
            response.setHl7Message(context.getServiceContext().getResponseMessage().encode());
        }
        catch (HL7Exception e) {
            throw new HubControllerFault(e.getMessage());
        }
        // Camel end
        // End of where I think the Camel components take over
        //SubmitSingleMessageResponse response = context.getSubmitSingleMessageResponse();
        response.setSchema(SoapMessage.HUB_NS);	// Shift from client to Hub Schema
        response.getHubHeader().setDestinationId(dest.getDestId());
        String uri = dest.getDestinationUri();
        response.getHubHeader().setDestinationUri(uri);
        ResponseEntity<?> result = checkResponseEntitySize(new ResponseEntity<>(response, HttpStatus.OK));
        return result;
    }

    private Message parseHl7v2Message(String rawHl7Message) throws HL7Exception {
        PipeParser parser;
        try (DefaultHapiContext context = new DefaultHapiContext()) {
            // This replacement just here because my SOAP client is messing w/ EOL characters
            rawHl7Message = rawHl7Message.replace("\n", "\r");
            context.setValidationContext(new NoValidation());
            parser = context.getPipeParser();
        } catch (IOException e) {
            throw new HL7Exception(e);
        }

        return parser.parse(rawHl7Message);
    }

    private ServiceContext getServiceContext(UUID organization, String incomingMessage) throws Fault {
        try {
            return new ServiceContext(organization,
                    "izgts:IISHubService",
                    "izghub:IISHubService",
                    DataType.HL7V2,
                    incomingMessage);
        }
        catch (HL7Exception e) {
            throw new HubControllerFault(e.getMessage());
        }
    }

    // TODO implement logging
    private void logDestination(IDestination dest) {
    }

    @Override
    protected IDestination getDestination(String destinationId) throws UnknownDestinationFault {
        IDestinationId destinationIdObject = new DestinationId();
        destinationIdObject.setDestId(destinationId);
        destinationIdObject.setDestType(0);

        IDestination hubDestination = new Destination();
        hubDestination.setId(destinationIdObject);
        hubDestination.setDestUri("https://localhost/IISHubService");

        return hubDestination;
    }

    @Override
    protected void checkCredentials(HasCredentials s) throws SecurityFault {

    }

    @Operation(
            summary = "Post a message to the SOAP Interface",
            description = "Send a request to the SOAP Interface for IZ Gateway"
    )
    @ApiResponses({@ApiResponse(
            responseCode = "200",
            description = "The request completed normally",
            content = {@Content(
                    mediaType = "application/xml"
            )}
    ), @ApiResponse(
            responseCode = "500",
            description = "A fault occured while processing the request",
            content = {@Content}
    )})
    @PostMapping(
            produces = {"application/soap+xml", "application/soap", "application/xml", "text/xml", "text/plain", "text/html"}
    )
    @Override
    public ResponseEntity<?> submitSoapRequest(@RequestBody SoapMessage soapMessage, @Schema(description = "Throws the fault specified in the header parameter") @RequestHeader(value = "X-IIS-Hub-Dev-Action",required = false) String devAction) {
        // TODO: This is a placeholder for the real EVENTID.  Paul to ask about how transaction data is set initially.
        TransactionData t = new TransactionData("TODO: A Real EVENTID");
        RequestContext.setTransactionData(t);

        return super.submitSoapRequest(soapMessage, devAction);
    }
}
