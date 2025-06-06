package gov.cdc.izgateway.xform.endpoints.hub;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.service.IMessageHeaderService;
import gov.cdc.izgateway.soap.fault.Fault;
import gov.cdc.izgateway.soap.fault.SecurityFault;
import gov.cdc.izgateway.soap.message.SoapMessage;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageRequest;
import gov.cdc.izgateway.xform.camel.constants.EndpointUris;
import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.enums.DataType;
import gov.cdc.izgateway.xform.security.Roles;
import gov.cdc.izgateway.xform.services.AccessControlService;
import gov.cdc.izgateway.xform.services.OrganizationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.UUID;

@RestController
@RolesAllowed({Roles.XFORM_SENDING_SYSTEM, Roles.ADMIN})
@RequestMapping("/IISService")
@Lazy(false)
@Slf4j
public class IISController extends BaseController {
    public IISController(
            IMessageHeaderService mshService,
            AccessControlRegistry registry,
            ProducerTemplate producerTemplate,
            OrganizationService organizationService,
            AccessControlService accessControlService
    ) {
        super(mshService, SoapMessage.IIS2014_NS, "cdc-iis.wsdl", Arrays.asList(SoapMessage.IIS2014_NS), producerTemplate, organizationService, accessControlService);
        registry.register(this);
    }

    @Override
    protected ResponseEntity<?> submitSingleMessage(SubmitSingleMessageRequest submitSingleMessage, String destinationId) throws Fault {

        return super.submitSingleMessage(submitSingleMessage, EndpointUris.DIRECT_IIS_PIPELINE);
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
    public ResponseEntity<?> submitSoapRequest(@RequestBody SoapMessage soapMessage, @Schema(description = "Throws the fault specified in the header parameter") @RequestHeader(value = "X-IIS-Hub-Dev-Action",required = false) String devAction) throws SecurityFault {
        return super.submitSoapRequest(soapMessage, devAction);
    }

    protected ServiceContext createServiceContext(UUID organization, SubmitSingleMessageRequest submitSingleMessage) throws Fault {
        try {
            return new ServiceContext(organization,
                    EndpointUris.IZGTS_IISService,
                    EndpointUris.IIS_IISService,
                    DataType.HL7V2,
                    submitSingleMessage.getFacilityID(),
                    submitSingleMessage.getHl7Message());
        } catch (HL7Exception e) {
            throw new HubControllerFault(e);
        }
    }

}
