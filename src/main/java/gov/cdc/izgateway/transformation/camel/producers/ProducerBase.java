package gov.cdc.izgateway.transformation.camel.producers;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.logging.info.DestinationInfo;
import gov.cdc.izgateway.model.IDestination;
import gov.cdc.izgateway.model.IDestinationId;
import gov.cdc.izgateway.soap.fault.UnknownDestinationFault;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageResponse;
import gov.cdc.izgateway.transformation.camel.producers.iis.IISComponent;
import gov.cdc.izgateway.transformation.context.IZGTransformationContext;
import gov.cdc.izgateway.transformation.endpoints.hub.HubControllerFault;
import gov.cdc.izgateway.transformation.endpoints.hub.HubMessageSender;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.model.Destination;
import gov.cdc.izgateway.transformation.model.DestinationId;
import gov.cdc.izgateway.transformation.util.Hl7Utils;
import gov.cdc.izgateway.utils.SystemUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
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

}
