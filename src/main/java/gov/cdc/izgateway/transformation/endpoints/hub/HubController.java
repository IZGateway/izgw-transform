package gov.cdc.izgateway.transformation.endpoints.hub;

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
import gov.cdc.izgateway.soap.message.*;
import gov.cdc.izgateway.soap.net.MessageSender;
import gov.cdc.izgateway.transformation.endpoints.hub.forreview.Destination;
import gov.cdc.izgateway.transformation.endpoints.hub.forreview.DestinationId;
import gov.cdc.izgateway.transformation.endpoints.hub.forreview.HubMessageSender;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RolesAllowed({Roles.SOAP, Roles.ADMIN})
@RequestMapping("/IISHubService")
@Lazy(false)
public class HubController extends SoapControllerBase {
    private MessageSender messageSender;
    @Autowired
    public HubController(
            IMessageHeaderService mshService,
            HubMessageSender messageSender,
            AccessControlRegistry registry
    ) {
        // The base schema for HUB messages is still the iis-2014 schema, with the exception of HubHeader and certain faults.
        super(mshService, SoapMessage.IIS2014_NS, "cdc-iis-hub.wsdl", Arrays.asList(SoapMessage.HUB_NS, SoapMessage.IIS2014_NS));
        this.messageSender = messageSender;
        registry.register(this);

        // TODO: Paul - this is temporary until I have a better understanding of this
        TransactionData t = new TransactionData("TODO: A Real EVENTID 1");
        RequestContext.setTransactionData(t);
    }

    @Override
    protected ResponseEntity<?> submitSingleMessage(SubmitSingleMessageRequest submitSingleMessage, String destinationId) throws Fault {
        // TODO: This is a placeholder for the real EVENTID
        TransactionData t = new TransactionData("TODO: A Real EVENTID");
        RequestContext.setTransactionData(t);

        IDestination dest = getDestination(destinationId);
        logDestination(dest);

        checkMessage(submitSingleMessage);

        SubmitSingleMessageResponse response = messageSender.sendSubmitSingleMessage(dest, submitSingleMessage);
        response.setSchema(SoapMessage.HUB_NS);	// Shift from client to Hub Schema
        response.getHubHeader().setDestinationId(dest.getDestId());
        String uri = dest.getDestinationUri();
        response.getHubHeader().setDestinationUri(uri);
        ResponseEntity<?> result = checkResponseEntitySize(new ResponseEntity<>(response, HttpStatus.OK));
        return result;
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
