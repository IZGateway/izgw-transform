package gov.cdc.izgateway.xform.camel.producers.iis;

import gov.cdc.izgateway.xform.camel.producers.IZGComponent;
import gov.cdc.izgateway.xform.endpoints.hub.HubMessageSender;
import lombok.Getter;
import org.apache.camel.Endpoint;
import org.apache.camel.support.DefaultComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("iis")
@Getter
public class IISComponent extends DefaultComponent implements IZGComponent {

    @Value("${xform.destination.iis.uri}")
    private String destinationUri;

    @Value("${xform.destination.iis.id}")
    private String destinationId;

    @Value("${xform.destination.iis.type}")
    private int destinationType;

    private final HubMessageSender messageSender;

    @Autowired
    public IISComponent(HubMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        return new IISEndpoint(uri, this);
    }

}
