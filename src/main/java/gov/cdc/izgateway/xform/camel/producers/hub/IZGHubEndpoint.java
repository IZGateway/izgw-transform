package gov.cdc.izgateway.xform.camel.producers.hub;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.support.DefaultEndpoint;

/**
 * Camel endpoint that creates producers for the IZ Gateway Hub destination.
 */
public class IZGHubEndpoint extends DefaultEndpoint {
    /**
     * Creates the endpoint.
     * @param endpointUri the URI of the endpoint
     * @param component the component that owns the endpoint
     */
    public IZGHubEndpoint(String endpointUri, IZGHubComponent component) {
        super(endpointUri, component);
    }

    @Override
    public Producer createProducer() throws Exception {
        return new IZGHubProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        return null;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
