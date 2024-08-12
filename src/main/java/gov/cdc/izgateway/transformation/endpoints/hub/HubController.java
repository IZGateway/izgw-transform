package gov.cdc.izgateway.transformation.endpoints.hub;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.logging.RequestContext;
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
import gov.cdc.izgateway.transformation.context.HubWsdlTransformationContext;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.model.Destination;
import gov.cdc.izgateway.transformation.model.DestinationId;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.enums.DataType;
import gov.cdc.izgateway.transformation.services.OrganizationService;
import gov.cdc.izgateway.transformation.util.Hl7Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.UUID;

@RestController
@RolesAllowed({Roles.SOAP, Roles.ADMIN})
@RequestMapping("/IISHubService")
@Lazy(false)
@Slf4j
public class HubController extends SoapControllerBase {
    private final ProducerTemplate producerTemplate;
    private final OrganizationService organizationService;

    @Value("${transformationservice.destination}")
    private String destinationUri;

    @Autowired
    public HubController(
            IMessageHeaderService mshService,
            AccessControlRegistry registry,
            ProducerTemplate producerTemplate,
            OrganizationService organizationService
    ) {
        // The base schema for HUB messages is still the iis-2014 schema, with the exception of HubHeader and certain faults.
        super(mshService, SoapMessage.IIS2014_NS, "cdc-iis-hub.wsdl", Arrays.asList(SoapMessage.HUB_NS, SoapMessage.IIS2014_NS));
        this.producerTemplate = producerTemplate;
        this.organizationService = organizationService;

        registry.register(this);

    }

    @Override
    protected ResponseEntity<?> submitSingleMessage(SubmitSingleMessageRequest submitSingleMessage, String destinationId) throws Fault {
        UUID organization = organizationService.getOrganizationByCommonName(RequestContext.getSourceInfo().getCommonName()).getId();

        // TODO Potentially refactor to not extract single pieces
        ServiceContext serviceContext = getServiceContext(organization, submitSingleMessage.getHl7Message());
        serviceContext.setCurrentDirection(DataFlowDirection.REQUEST);

        HubWsdlTransformationContext context = new HubWsdlTransformationContext(serviceContext, submitSingleMessage);

        try {
            producerTemplate.sendBody("direct:izghubTransformerPipeline", context);
        }
        catch (CamelExecutionException e) {
            throw new HubControllerFault(e.getCause().getMessage());
        }

        try {
            context.getSubmitSingleMessageResponse().setHl7Message(serviceContext.getResponseMessage().encode());
        }
        catch (HL7Exception e) {
            throw new HubControllerFault(e.getMessage());
        }

        return checkResponseEntitySize(new ResponseEntity<>(context.getSubmitSingleMessageResponse(), HttpStatus.OK));
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

    @Override
    protected IDestination getDestination(String destinationId) throws UnknownDestinationFault {
        IDestinationId destinationIdObject = new DestinationId();
        destinationIdObject.setDestId(destinationId);
        destinationIdObject.setDestType(0);

        IDestination hubDestination = new Destination();
        hubDestination.setId(destinationIdObject);

        hubDestination.setDestUri(destinationUri);

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

        return super.submitSoapRequest(soapMessage, devAction);
    }
}
