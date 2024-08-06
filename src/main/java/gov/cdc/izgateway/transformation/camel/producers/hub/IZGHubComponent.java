package gov.cdc.izgateway.transformation.camel.producers.hub;

import gov.cdc.izgateway.model.IDestination;
import gov.cdc.izgateway.model.IDestinationId;
import gov.cdc.izgateway.soap.fault.UnknownDestinationFault;
import gov.cdc.izgateway.transformation.model.Destination;
import gov.cdc.izgateway.transformation.model.DestinationId;
import gov.cdc.izgateway.transformation.endpoints.hub.HubMessageSender;
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
    // The IZ Gateway Hub Destination ID will always be 0.
    private static final String HUB_DESTINATION_ID = "0";

    // The IZ Gateway Hub Destination Type will always be 0.
    private static final int HUB_DESTINATION_TYPE = 0;

    @Value("${transformationservice.destination}")
    private String destinationUri;

    private final HubMessageSender messageSender;

    @Autowired
    public IZGHubComponent(HubMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        return new IZGHubEndpoint(uri, this);
    }

    /**
     * The Transformation Service has only one IZ Gateway Destination.  This destination is the IZ Gateway Hub.
     * @return
     * @throws UnknownDestinationFault
     */
    IDestination getDestination() throws UnknownDestinationFault {
        IDestinationId destinationIdObject = new DestinationId();
        destinationIdObject.setDestId(HUB_DESTINATION_ID);
        destinationIdObject.setDestType(HUB_DESTINATION_TYPE);

        IDestination hubDestination = new Destination();
        hubDestination.setId(destinationIdObject);
        hubDestination.setDestUri(destinationUri);

        return hubDestination;
    }

}