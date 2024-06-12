package gov.cdc.izgateway.transformation.camel.producers.hub;

import gov.cdc.izgateway.transformation.endpoints.hub.forreview.HubMessageSender;
import lombok.Getter;
import org.apache.camel.Endpoint;
import org.apache.camel.support.DefaultComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("izghub")
@Getter
public class IZGHubComponent extends DefaultComponent {
    private HubMessageSender messageSender;

    @Autowired
    public IZGHubComponent(HubMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        return new IZGHubEndpoint(uri, this);
    }
}