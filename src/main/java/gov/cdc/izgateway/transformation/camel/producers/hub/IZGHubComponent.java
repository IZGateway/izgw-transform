package gov.cdc.izgateway.transformation.camel.producers.hub;

import gov.cdc.izgateway.model.IDestination;
import gov.cdc.izgateway.model.IDestinationId;
import gov.cdc.izgateway.soap.fault.UnknownDestinationFault;
import gov.cdc.izgateway.transformation.camel.producers.IZGComponent;
import gov.cdc.izgateway.transformation.model.Destination;
import gov.cdc.izgateway.transformation.model.DestinationId;
import gov.cdc.izgateway.transformation.endpoints.hub.HubMessageSender;
import gov.cdc.izgateway.utils.SystemUtils;
import lombok.Getter;
import org.apache.camel.Endpoint;
import org.apache.camel.support.DefaultComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("izghub")
@Getter
public class IZGHubComponent extends DefaultComponent implements IZGComponent {

    @Value("${transformationservice.destination.hub.uri}")
    private String destinationUri;

    @Value("${transformationservice.destination.hub.id}")
    private String destinationId;

    @Value("${transformationservice.destination.hub.type}")
    private int destinationType;

    private final HubMessageSender messageSender;

    @Autowired
    public IZGHubComponent(HubMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        return new IZGHubEndpoint(uri, this);
    }

}