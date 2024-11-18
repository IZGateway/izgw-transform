package gov.cdc.izgateway.transformation.camel.producers.iis;

import gov.cdc.izgateway.model.IDestination;
import gov.cdc.izgateway.model.IDestinationId;
import gov.cdc.izgateway.soap.fault.UnknownDestinationFault;
import gov.cdc.izgateway.transformation.camel.producers.IZGComponent;
import gov.cdc.izgateway.transformation.endpoints.hub.HubMessageSender;
import gov.cdc.izgateway.transformation.model.Destination;
import gov.cdc.izgateway.transformation.model.DestinationId;
import gov.cdc.izgateway.utils.SystemUtils;
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

    @Value("${transformationservice.destination.iis.uri}")
    private String destinationUri;

    @Value("${transformationservice.destination.iis.id}")
    private String destinationId;

    @Value("${transformationservice.destination.iis.type}")
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