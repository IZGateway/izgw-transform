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
import gov.cdc.izgateway.transformation.logging.advice.XformAdviceCollector;
import gov.cdc.izgateway.transformation.model.Destination;
import gov.cdc.izgateway.transformation.model.DestinationId;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.enums.DataType;
import gov.cdc.izgateway.transformation.model.Organization;
import gov.cdc.izgateway.transformation.services.OrganizationService;
import gov.cdc.izgateway.transformation.services.XformAccessControlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RolesAllowed({Roles.SOAP, Roles.ADMIN})
@RequestMapping("/IISHubService")
@Lazy(false)
@Slf4j
public class HubController extends SoapControllerBase {
    private final ProducerTemplate producerTemplate;
    private final OrganizationService organizationService;
    private final XformAccessControlService accessControlService;

    @Value("${transformationservice.destination}")
    private String destinationUri;

    @Autowired
    public HubController(
            IMessageHeaderService mshService,
            AccessControlRegistry registry,
            ProducerTemplate producerTemplate,
            OrganizationService organizationService,
            XformAccessControlService accessControlService
    ) {
        // The base schema for HUB messages is still the iis-2014 schema, with the exception of HubHeader and certain faults.
        super(mshService, SoapMessage.IIS2014_NS, "cdc-iis-hub.wsdl", Arrays.asList(SoapMessage.HUB_NS, SoapMessage.IIS2014_NS));
        this.producerTemplate = producerTemplate;
        this.organizationService = organizationService;
        this.accessControlService = accessControlService;

        registry.register(this);

    }

    @Override
    protected ResponseEntity<?> submitSingleMessage(SubmitSingleMessageRequest submitSingleMessage, String destinationId) throws Fault {
        UUID organization = getOrganization(RequestContext.getSourceInfo().getCommonName()).getId();
        HubWsdlTransformationContext context = createHubWsdlTransformationContext(organization, RequestContext.getSourceInfo().getFacilityId(), submitSingleMessage);

        try {
            producerTemplate.sendBody("direct:izghubTransformerPipeline", context);

            if ( XformAdviceCollector.getTransactionData().getPipelineAdvice().isRequestTransformed()) {
                context.getSubmitSingleMessageResponse().getXformHeader().setTransformedRequest(XformAdviceCollector.getTransactionData().getPipelineAdvice().getTransformedRequest());
            }
            if ( XformAdviceCollector.getTransactionData().getPipelineAdvice().isResponseTransformed())
                context.getSubmitSingleMessageResponse().getXformHeader().setOriginalResponse(XformAdviceCollector.getTransactionData().getPipelineAdvice().getResponse());

            context.getSubmitSingleMessageResponse().setHl7Message(context.getServiceContext().getResponseMessage().encode());
        } catch (CamelExecutionException | HL7Exception e) {
            throw new HubControllerFault(e.getCause().getMessage());
        }

        return checkResponseEntitySize(new ResponseEntity<>(context.getSubmitSingleMessageResponse(), HttpStatus.OK));
    }

    private Organization getOrganization(String commonName) throws Fault {
        Organization organization = organizationService.getOrganizationByCommonName(commonName);

        if (organization == null) {
            throw new HubControllerFault("Organization not found for common name: " + commonName);
        }

        return checkOrganizationOverride(organization);
    }

    /**
     * In some scenarios such as testing, it is desirable that the incoming request use a different Organization
     * than the one related to the client-side certificate.  If the Organization is an admin, then the incoming
     * request is checked for an x-xform-organization header.  If the header is present, the organization is set to
     * this header value.
     *
     * @param organization
     * @return
     * @throws Fault
     */
    private Organization checkOrganizationOverride(Organization organization) throws Fault {
        if (RequestContext.getHttpHeaders() != null
                && RequestContext.getHttpHeaders().containsKey("x-xform-organization")
                && accessControlService.isUserInRole(organization.getId(), XformAccessControlService.ADMIN_ROLE)) {

            Map<String, List<String>> headers = RequestContext.getHttpHeaders();
            String orgId = headers.get("x-xform-organization").get(0);
            Organization organizationOverride = organizationService.getObject(UUID.fromString(orgId));
            if (organizationOverride == null) {
                throw new HubControllerFault("Organization not found for organizationId: " + orgId);
            }
            return organizationOverride;
        }

        return organization;
    }

    private HubWsdlTransformationContext createHubWsdlTransformationContext(UUID organization, String facilityId, SubmitSingleMessageRequest submitSingleMessage) throws Fault {
        ServiceContext serviceContext = createServiceContext(organization, facilityId, submitSingleMessage.getHl7Message());
        serviceContext.setCurrentDirection(DataFlowDirection.REQUEST);

        return new HubWsdlTransformationContext(serviceContext, submitSingleMessage);
    }

    private ServiceContext createServiceContext(UUID organization, String facilityId, String incomingMessage) throws Fault {
        try {
            return new ServiceContext(organization,
                    "izgts:IISHubService",
                    "izghub:IISHubService",
                    DataType.HL7V2,
                    facilityId,
                    incomingMessage);
        } catch (HL7Exception e) {
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
        // This is not used for Xform Service yet
    }

    @Operation(
            summary = "Post a message to the SOAP Interface",
            description = "Send a request to the SOAP Interface for IZ Gateway"
    )
    @ApiResponse(
        responseCode = "200",
        description = "The request completed normally",
        content = {
            @Content(mediaType = "application/xml")
        }
    )
    @ApiResponse(
            responseCode = "500",
            description = "A fault occured while processing the request",
            content = {@Content}
    )
    @PostMapping(
            produces = {"application/soap+xml", "application/soap", "application/xml", "text/xml", "text/plain", "text/html"}
    )
    @Override
    public ResponseEntity<?> submitSoapRequest(@RequestBody SoapMessage soapMessage, @Schema(description = "Throws the fault specified in the header parameter") @RequestHeader(value = "X-IIS-Hub-Dev-Action",required = false) String devAction) {

        return super.submitSoapRequest(soapMessage, devAction);
    }
}
