package gov.cdc.izgateway.xform.endpoints.hub;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.model.IDestination;
import gov.cdc.izgateway.model.IDestinationId;
import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.service.IMessageHeaderService;
import gov.cdc.izgateway.soap.fault.Fault;
import gov.cdc.izgateway.soap.fault.SecurityFault;
import gov.cdc.izgateway.soap.fault.UnknownDestinationFault;
import gov.cdc.izgateway.soap.message.SoapMessage;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageRequest;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageResponse;
import gov.cdc.izgateway.xform.camel.constants.EndpointUris;
import gov.cdc.izgateway.xform.context.IZGXformContext;
import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.logging.advice.PipelineAdvice;
import gov.cdc.izgateway.xform.logging.advice.XformAdviceCollector;
import gov.cdc.izgateway.xform.model.Destination;
import gov.cdc.izgateway.xform.model.DestinationId;
import gov.cdc.izgateway.xform.enums.DataFlowDirection;
import gov.cdc.izgateway.xform.enums.DataType;
import gov.cdc.izgateway.xform.security.Roles;
import gov.cdc.izgateway.xform.services.AccessControlService;
import gov.cdc.izgateway.xform.services.OrganizationService;
import gov.cdc.izgateway.xform.util.Hl7Utils;
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
import java.util.UUID;

@RestController
@RolesAllowed({Roles.ADMIN, Roles.XFORM_SENDING_SYSTEM})
@RequestMapping("/IISHubService")
@Lazy(false)
@Slf4j
public class HubController extends BaseController /*SoapControllerBase*/ {
    private final ProducerTemplate producerTemplate;

    @Value("${xform.destination.hub.uri:}")
    private String destinationUri;

    @Autowired
    public HubController(
            IMessageHeaderService mshService,
            AccessControlRegistry registry,
            ProducerTemplate producerTemplate,
            OrganizationService organizationService,
            AccessControlService accessControlService
    ) {
        // The base schema for HUB messages is still the iis-2014 schema, with the exception of HubHeader and certain faults.
        super(mshService, SoapMessage.IIS2014_NS, "cdc-iis.wsdl", Arrays.asList(SoapMessage.IIS2014_NS), producerTemplate, organizationService, accessControlService);
        this.producerTemplate = producerTemplate;
        registry.register(this);

    }

    @Override
    protected ResponseEntity<?> submitSingleMessage(SubmitSingleMessageRequest submitSingleMessage, String destinationId) throws Fault {
        RequestContext.getDestinationInfo().setId(destinationId);
        UUID organization = getOrganization(RequestContext.getSourceInfo().getCommonName()).getId();

        if (isLoopback()) {
        	return loopbackSingleMessage(submitSingleMessage);
        }
        	
        IZGXformContext context = createXformContext(organization, submitSingleMessage);

    	String transformedRequest = null;
    	SubmitSingleMessageResponse response = null;
        try {
    		producerTemplate.sendBody(EndpointUris.DIRECT_HUB_PIPELINE, context);
        	PipelineAdvice advice = XformAdviceCollector.getTransactionData().getPipelineAdvice();
    		response = context.getSubmitSingleMessageResponse(); 
        	
            if (advice != null) {
                if (advice.isRequestTransformed()) {
                	transformedRequest = advice.getTransformedRequest();
                	if (response != null) {
                		response.getXformHeader().setTransformedRequest(transformedRequest);
                	}
                }
                if (response != null && advice.isResponseTransformed()) {
                	response.getXformHeader().setOriginalResponse(advice.getResponse());
                }
            }
            
        	response.setHl7Message(context.getServiceContext().getResponseMessage().encode());
        } catch (CamelExecutionException | HL7Exception e) {
            throw new HubControllerFault(e.getCause());
        }
        
        return checkResponseEntitySize(new ResponseEntity<>(response, HttpStatus.OK));
    }

    /**
     * @return true if this message is requesting loopback processing, false otherwise.
     */
    private boolean isLoopback() {
        List<String> headers = RequestContext.getHttpHeaders().get("x-loopback");
        return headers != null && headers.stream().anyMatch(v -> "true".equalsIgnoreCase(v));
	}

	private ResponseEntity<?> loopbackSingleMessage(SubmitSingleMessageRequest submitSingleMessage) throws Fault  {
        UUID organization = getOrganization(RequestContext.getSourceInfo().getCommonName()).getId();
    	IZGXformContext context = createXformContext(organization, submitSingleMessage);

    	String transformedRequest = null;
    	boolean isResponse = submitSingleMessage.getHl7Message().contains("MSA|");
    	SubmitSingleMessageResponse response = isResponse ? 
    			new SubmitSingleMessageResponse(submitSingleMessage, getMessageNamespace(), true) :
    			null;
    	try {
        	if (isResponse) {
        		// Setup for the Response Path
            	response.setHl7Message(submitSingleMessage.getHl7Message());
        		context.setSubmitSingleMessageResponse(response);
        		context.getServiceContext().setCurrentDirection(DataFlowDirection.RESPONSE);
        		context.getServiceContext().setResponseMessage(Hl7Utils.parseHl7v2Message(response.getHl7Message()));
        	}
        	
    		producerTemplate.sendBody(EndpointUris.LOOPBACK_HUB_PIPELINE, context);
        	PipelineAdvice advice = XformAdviceCollector.getTransactionData().getPipelineAdvice();
        	
            if (advice != null) {
                if (advice.isRequestTransformed()) {
                	transformedRequest = advice.getTransformedRequest();
            		response.getXformHeader().setTransformedRequest(transformedRequest);
                }
                if (advice.isResponseTransformed()) {
                	response.getXformHeader().setOriginalResponse(advice.getResponse());
                }
            }
            if (!isResponse) {
            	response.setHl7Message(transformedRequest);
            } else {
            	response.setHl7Message(context.getServiceContext().getCurrentMessage().toString());
            }
        } catch (CamelExecutionException e) {
            throw new HubControllerFault(e.getCause());
        } catch (HL7Exception e) {
			throw new HubControllerFault(e.getCause());
		}
        
        return checkResponseEntitySize(new ResponseEntity<>(response, HttpStatus.OK));
	}

	protected ServiceContext createServiceContext(UUID organization, SubmitSingleMessageRequest submitSingleMessage) throws Fault {
        try {
            return new ServiceContext(organization,
                    EndpointUris.IZGTS_IISHubService,
                    EndpointUris.IZGHUB_IISHubService,
                    DataType.HL7V2,
                    submitSingleMessage.getFacilityID(),
                    submitSingleMessage.getHl7Message());
        } catch (HL7Exception e) {
            throw new HubControllerFault(e);
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
}
