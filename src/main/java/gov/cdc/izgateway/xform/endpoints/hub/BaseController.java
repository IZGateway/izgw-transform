package gov.cdc.izgateway.xform.endpoints.hub;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.service.IMessageHeaderService;
import gov.cdc.izgateway.soap.SoapControllerBase;
import gov.cdc.izgateway.soap.fault.Fault;
import gov.cdc.izgateway.soap.fault.SecurityFault;
import gov.cdc.izgateway.soap.message.HasCredentials;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageRequest;
import gov.cdc.izgateway.xform.context.IZGXformContext;
import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.enums.DataFlowDirection;
import gov.cdc.izgateway.xform.logging.advice.XformAdviceCollector;
import gov.cdc.izgateway.xform.model.Organization;
import gov.cdc.izgateway.xform.security.Roles;
import gov.cdc.izgateway.xform.services.AccessControlService;
import gov.cdc.izgateway.xform.services.OrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.ProducerTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Base class for the SOAP controllers. It sends inbound messages through a Camel pipeline route.
 */
@Slf4j
public abstract class BaseController extends SoapControllerBase {
    /** Sends the message context to the Camel pipeline route. */
    protected final ProducerTemplate producerTemplate;
    /** Finds the organization of the sender. */
    protected final OrganizationService organizationService;
    /** The service that checks access to request paths. */
    protected final AccessControlService accessControlService;

    /**
     * Creates the controller.
     *
     * @param mshService the message header service
     * @param namespace the SOAP namespace of the service
     * @param wsdl the WSDL of the service
     * @param additionalNamespaces other namespaces that the service accepts
     * @param producerTemplate sends the message context to the Camel pipeline route
     * @param organizationService finds the organization of the sender
     * @param accessControlService the service that checks access to request paths
     */
    protected BaseController(
            IMessageHeaderService mshService,
            String namespace,
            String wsdl,
            List<String> additionalNamespaces,
            ProducerTemplate producerTemplate,
            OrganizationService organizationService,
            AccessControlService accessControlService
    ) {
        super(mshService, namespace, wsdl, additionalNamespaces);
        this.producerTemplate = producerTemplate;
        this.organizationService = organizationService;
        this.accessControlService = accessControlService;
    }

    protected ResponseEntity<?> submitSingleMessage(SubmitSingleMessageRequest submitSingleMessage, String pipeline) throws Fault {

        UUID organization = getOrganization(RequestContext.getSourceInfo().getCommonName()).getId();
        IZGXformContext context = createXformContext(organization, submitSingleMessage);

        try {
            producerTemplate.sendBody(pipeline, context);

            if (XformAdviceCollector.getTransactionData().getPipelineAdvice() != null) {
                if (XformAdviceCollector.getTransactionData().getPipelineAdvice().isRequestTransformed())
                    context.getSubmitSingleMessageResponse().getXformHeader().setTransformedRequest(XformAdviceCollector.getTransactionData().getPipelineAdvice().getTransformedRequest());
                if (XformAdviceCollector.getTransactionData().getPipelineAdvice().isResponseTransformed())
                    context.getSubmitSingleMessageResponse().getXformHeader().setOriginalResponse(XformAdviceCollector.getTransactionData().getPipelineAdvice().getResponse());
            }

            context.getSubmitSingleMessageResponse().setHl7Message(context.getServiceContext().getResponseMessage().encode());
            context.getSubmitSingleMessageResponse().updateAction(isHubWsdl());

        } catch (CamelExecutionException | HL7Exception e) {
            throw new HubControllerFault(e.getCause());
        }

        return checkResponseEntitySize(new ResponseEntity<>(context.getSubmitSingleMessageResponse(), HttpStatus.OK));
    }

    /**
     * Returns the organization for a certificate common name, or the override organization from the
     * x-xform-organization header if the user is an administrator.
     *
     * @param commonName the common name of the client certificate
     * @return the organization
     * @throws Fault if no organization is found
     */
    protected Organization getOrganization(String commonName) throws Fault {
        Organization organization = checkOrganizationOverride(organizationService.getOrganizationByCommonName(commonName));
        if (organization == null) {
            throw new HubControllerFault("Organization not found for " + commonName);
        }
        log.debug("{} found for {}", organization.getOrganizationName(), commonName);
        return organization;
    }

    /**
     * Returns the organization from the x-xform-organization header if the user has the ADMIN role
     * and the header is present; otherwise returns the given organization.
     *
     * @param organization the organization found for the sender
     * @return the organization to use
     * @throws Fault if the organization in the header is not found
     */
    protected Organization checkOrganizationOverride(Organization organization) throws Fault {
        if (RequestContext.getHttpHeaders() != null
                && RequestContext.getPrincipal().getRoles().contains(Roles.ADMIN)
                && RequestContext.getHttpHeaders().containsKey("x-xform-organization")) {

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

    /**
     * Creates the transformation context for a request, with the direction set to REQUEST.
     *
     * @param organization the organization identifier
     * @param submitSingleMessage the inbound request
     * @return the transformation context
     * @throws Fault if the service context cannot be created
     */
    protected IZGXformContext createXformContext(UUID organization, SubmitSingleMessageRequest submitSingleMessage) throws Fault {
        ServiceContext serviceContext = createServiceContext(organization, submitSingleMessage);
        serviceContext.setCurrentDirection(DataFlowDirection.REQUEST);

        return new IZGXformContext(serviceContext, submitSingleMessage);
    }

    /**
     * Creates the service context for a request.
     *
     * @param organization the organization identifier
     * @param submitSingleMessage the inbound request
     * @return the service context
     * @throws Fault if the service context cannot be created
     */
    abstract protected ServiceContext createServiceContext(UUID organization, SubmitSingleMessageRequest submitSingleMessage) throws Fault;

    @Override
    protected void checkCredentials(HasCredentials s) throws SecurityFault {
        // This is not used for Xform Service yet
    }
}
