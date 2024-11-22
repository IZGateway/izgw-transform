package gov.cdc.izgateway.xform.endpoints.hub;

import gov.cdc.izgateway.configuration.AppProperties;
import gov.cdc.izgateway.configuration.ClientConfiguration;
import gov.cdc.izgateway.configuration.SenderConfig;
import gov.cdc.izgateway.configuration.ServerConfiguration;
import gov.cdc.izgateway.security.ClientTlsSupport;
import gov.cdc.izgateway.service.impl.EndpointStatusService;
import gov.cdc.izgateway.soap.net.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HubMessageSender extends MessageSender {
    private EndpointStatusService statusService;

    @Autowired
    public HubMessageSender(final ServerConfiguration serverConfig, final SenderConfig senderConfig, final ClientConfiguration clientConfig, final ClientTlsSupport tlsSupport, final EndpointStatusService statusService, final AppProperties app
    ) {
        super(serverConfig, senderConfig, clientConfig, tlsSupport, statusService, app);
        this.statusService = statusService;
    }

}
