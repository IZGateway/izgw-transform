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

@Slf4j
public abstract class ProducerBase extends DefaultProducer {

    protected ProducerBase(Endpoint endpoint) {
        super(endpoint);
    }

    public void setDestinationInfoFromDestination(DestinationInfo info, IDestination route) {
        if (route == null) {
            info.setUrl(null);
            info.setId(null);
            return;
        }
        info.setUrl(route.getDestUri());
        info.setId(route.getDestId());
    }

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
