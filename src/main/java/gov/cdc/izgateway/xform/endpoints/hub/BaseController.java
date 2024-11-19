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
import gov.cdc.izgateway.xform.enums.DataType;
import gov.cdc.izgateway.xform.logging.advice.XformAdviceCollector;
import gov.cdc.izgateway.xform.model.Organization;
import gov.cdc.izgateway.xform.services.OrganizationService;
import gov.cdc.izgateway.xform.services.XformAccessControlService;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.ProducerTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public abstract class BaseController extends SoapControllerBase {
    protected final ProducerTemplate producerTemplate;
    protected final OrganizationService organizationService;
    protected final XformAccessControlService accessControlService;

    protected BaseController(
            IMessageHeaderService mshService,
            String namespace,
            String wsdl,
            List<String> additionalNamespaces,
            ProducerTemplate producerTemplate,
            OrganizationService organizationService,
            XformAccessControlService accessControlService
    ) {
        super(mshService, namespace, wsdl, additionalNamespaces);
        this.producerTemplate = producerTemplate;
        this.organizationService = organizationService;
        this.accessControlService = accessControlService;
    }

    protected ResponseEntity<?> submitSingleMessage(SubmitSingleMessageRequest submitSingleMessage, String pipeline) throws Fault {
        UUID organization = getOrganization(RequestContext.getSourceInfo().getCommonName()).getId();
        IZGXformContext context = createHubWsdlXformContext(organization, submitSingleMessage);

        try {
            producerTemplate.sendBody(pipeline, context);

            if (XformAdviceCollector.getTransactionData().getPipelineAdvice() != null) {
                if (XformAdviceCollector.getTransactionData().getPipelineAdvice().isRequestTransformed())
                    context.getSubmitSingleMessageResponse().getXformHeader().setTransformedRequest(XformAdviceCollector.getTransactionData().getPipelineAdvice().getTransformedRequest());
                if (XformAdviceCollector.getTransactionData().getPipelineAdvice().isResponseTransformed())
                    context.getSubmitSingleMessageResponse().getXformHeader().setOriginalResponse(XformAdviceCollector.getTransactionData().getPipelineAdvice().getResponse());
            }

            context.getSubmitSingleMessageResponse().setHl7Message(context.getServiceContext().getResponseMessage().encode());
        } catch (CamelExecutionException | HL7Exception e) {
            throw new HubControllerFault(e.getCause().getMessage());
        }

        return checkResponseEntitySize(new ResponseEntity<>(context.getSubmitSingleMessageResponse(), HttpStatus.OK));
    }

    protected Organization getOrganization(String commonName) throws Fault {
        Organization organization = checkOrganizationOverride(organizationService.getOrganizationByCommonName(commonName));
        if (organization == null) {
            throw new HubControllerFault("Organization not found for " + commonName);
        }
        return organization;
    }

    protected Organization checkOrganizationOverride(Organization organization) throws Fault {
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

    protected IZGXformContext createHubWsdlXformContext(UUID organization, SubmitSingleMessageRequest submitSingleMessage) throws Fault {
        ServiceContext serviceContext = createServiceContext(organization, submitSingleMessage);
        serviceContext.setCurrentDirection(DataFlowDirection.REQUEST);

        return new IZGXformContext(serviceContext, submitSingleMessage);
    }

    protected ServiceContext createServiceContext(UUID organization, SubmitSingleMessageRequest submitSingleMessage) throws Fault {
        try {
            return new ServiceContext(organization,
                    "izgts:IISHubService",
                    "izghub:IISHubService",
                    DataType.HL7V2,
                    submitSingleMessage.getFacilityID(),
                    submitSingleMessage.getHl7Message());
        } catch (HL7Exception e) {
            throw new HubControllerFault(e.getMessage());
        }
    }

    @Override
    protected void checkCredentials(HasCredentials s) throws SecurityFault {
        // This is not used for Xform Service yet
    }
}