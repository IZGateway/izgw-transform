package gov.cdc.izgateway.transformation.camel.producers.hub;

import gov.cdc.izgateway.model.IDestination;
import gov.cdc.izgateway.model.IDestinationId;
import gov.cdc.izgateway.soap.fault.UnknownDestinationFault;
import gov.cdc.izgateway.transformation.endpoints.hub.forreview.Destination;
import gov.cdc.izgateway.transformation.endpoints.hub.forreview.DestinationId;
import gov.cdc.izgateway.transformation.endpoints.hub.forreview.HubMessageSender;
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
public class IZGHubComponent extends DefaultComponent {
    @Value("${transformationservice.destination}")
    private String destinationUri;

    private HubMessageSender messageSender;

    @Autowired
    public IZGHubComponent(HubMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        return new IZGHubEndpoint(uri, this);
    }

    IDestination getDestination(String destinationId) throws UnknownDestinationFault {
        DestinationId destinationIdObject = new DestinationId();
        destinationIdObject.setDestId(destinationId);
        destinationIdObject.setDestType(SystemUtils.getDestType());

        Destination hubDestination = new Destination();
        hubDestination.setId(destinationIdObject);
        hubDestination.setDestinationId(destinationId);
        hubDestination.setDestUri(destinationUri);
        hubDestination.setDestType(SystemUtils.getDestTypeAsString());
        hubDestination.setDestTypeId(SystemUtils.getDestType());
        return hubDestination;
    }

}