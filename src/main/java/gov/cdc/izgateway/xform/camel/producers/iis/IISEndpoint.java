package gov.cdc.izgateway.xform.camel.producers.iis;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.support.DefaultEndpoint;

/**
 * Camel endpoint that creates producers for the IIS destination.
 */
public class IISEndpoint extends DefaultEndpoint {
    /**
     * Creates the endpoint.
     * @param endpointUri the URI of the endpoint
     * @param component the component that owns the endpoint
     */
    public IISEndpoint(String endpointUri, IISComponent component) {
        super(endpointUri, component);
    }

    @Override
    public Producer createProducer() throws Exception {
        return new IISProducer(this);
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
