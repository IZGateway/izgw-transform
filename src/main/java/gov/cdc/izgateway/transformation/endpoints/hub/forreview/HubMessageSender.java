package gov.cdc.izgateway.transformation.endpoints.hub.forreview;

import gov.cdc.izgateway.configuration.AppProperties;
import gov.cdc.izgateway.configuration.ClientConfiguration;
import gov.cdc.izgateway.configuration.SenderConfig;
import gov.cdc.izgateway.configuration.ServerConfiguration;
import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.model.IDestination;
import gov.cdc.izgateway.model.IEndpointStatus;
import gov.cdc.izgateway.security.ClientTlsSupport;
import gov.cdc.izgateway.service.impl.EndpointStatusService;
import gov.cdc.izgateway.soap.fault.DestinationConnectionFault;
import gov.cdc.izgateway.soap.fault.Fault;
import gov.cdc.izgateway.soap.message.SoapMessage;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageRequest;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageResponse;
import gov.cdc.izgateway.soap.net.MessageSender;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// TODO Paul - discuss HubMessageSender - can we modify the MessageSender in core to handle the "hub header clear method call" toBeSent.getHubHeader().clear();
// 6/10 - change core MessageSender to only clear the hub header if the destination is not a hub destination
@Component
public class HubMessageSender extends MessageSender {
    private EndpointStatusService statusService;

    @Autowired
    public HubMessageSender(final ServerConfiguration serverConfig, final SenderConfig senderConfig, final ClientConfiguration clientConfig, final ClientTlsSupport tlsSupport, final EndpointStatusService statusService, final AppProperties app
    ) {
        super(serverConfig, senderConfig, clientConfig, tlsSupport, statusService, app);
        this.statusService = statusService;
    }

    @Override
    public SubmitSingleMessageResponse sendSubmitSingleMessage(
            IDestination dest,
            SubmitSingleMessageRequest submitSingleMessage
    ) throws Fault {

        IEndpointStatus status = checkDestinationStatus(dest);
        SubmitSingleMessageRequest toBeSent =
                new SubmitSingleMessageRequest(submitSingleMessage, getSchemaToUse(dest), true);
        // Clear the hub header, we don't forward that.
        // TODO Paul - we do want the headers because we're sending to hub
        // toBeSent.getHubHeader().clear();
        copyCredentials(toBeSent, dest);
        int retryCount = 0;
        while (true) {
            try {
                SubmitSingleMessageResponse responseFromClient = sendMessage(SubmitSingleMessageResponse.class, dest, toBeSent);
                SubmitSingleMessageResponse toBeReturned = new SubmitSingleMessageResponse(responseFromClient, submitSingleMessage.getSchema(), true);
                toBeReturned.updateAction(true);  // Now a Hub Response
                RequestContext.getTransactionData().setRetries(retryCount);
                // TODO Paul updateStatus(status, dest, true);
                return toBeReturned;
            } catch (Fault f) {
                retryCount++;
                // TODO Paul checkRetries(dest, status, retryCount, f);
                // Log the fault and try again.
            }
        }


    }

    private IEndpointStatus checkDestinationStatus(IDestination dest) throws DestinationConnectionFault {

        // Check the circuit breaker
        IEndpointStatus status = statusService.getEndpointStatus(dest);
        return status;
    }
    private void copyCredentials(SubmitSingleMessageRequest toBeSent, IDestination dest) {
        if (StringUtils.isNotEmpty(dest.getUsername())) {
            toBeSent.setUsername(dest.getUsername());
        }
        if (StringUtils.isNotEmpty(dest.getPassword())) {
            toBeSent.setPassword(dest.getPassword());
        }
    }

    private String getSchemaToUse(IDestination dest) {
        return SoapMessage.HUB_NS;
    }
}
