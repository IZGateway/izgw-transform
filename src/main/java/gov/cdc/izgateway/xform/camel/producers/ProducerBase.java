package gov.cdc.izgateway.xform.camel.producers;

import gov.cdc.izgateway.logging.info.DestinationInfo;
import gov.cdc.izgateway.model.IDestination;
import gov.cdc.izgateway.model.IDestinationId;
import gov.cdc.izgateway.soap.fault.UnknownDestinationFault;
import gov.cdc.izgateway.xform.model.Destination;
import gov.cdc.izgateway.xform.model.DestinationId;
import gov.cdc.izgateway.utils.SystemUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Endpoint;
import org.apache.camel.support.DefaultProducer;

/**
 * Base class for Camel producers that send messages to a destination from an {@link IZGComponent}.
 */
@Slf4j
public abstract class ProducerBase extends DefaultProducer {

    /**
     * Creates the producer.
     * @param endpoint the endpoint that owns the producer
     */
    protected ProducerBase(Endpoint endpoint) {
        super(endpoint);
    }

    /**
     * Copies the URL and identifier of a destination into the logging destination information.
     * @param info the logging destination information to update
     * @param route the destination, or null to clear the URL and identifier
     */
    public void setDestinationInfoFromDestination(DestinationInfo info, IDestination route) {
        if (route == null) {
            info.setUrl(null);
            info.setId(null);
            return;
        }
        info.setUrl(route.getDestUri());
        info.setId(route.getDestId());
    }

    /**
     * Creates a destination from the settings of a component.
     * @param component the component that holds the destination settings
     * @return the new destination
     * @throws UnknownDestinationFault if the destination cannot be created
     */
    protected IDestination createDestination(IZGComponent component) throws UnknownDestinationFault {
        IDestinationId destinationIdObject = new DestinationId();
        destinationIdObject.setDestId(component.getDestinationId());
        destinationIdObject.setDestType(component.getDestinationType());

        Destination destination = new Destination();
        destination.setDestUri(component.getDestinationUri());
        destination.setDestId(component.getDestinationId());
        destination.setDestTypeId(SystemUtils.getDestType());
        destination.setId(destinationIdObject);

        return destination;
    }
}
