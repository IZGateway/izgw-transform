package gov.cdc.izgateway.transformation.camel.producers.hub;

import org.apache.camel.Endpoint;
import org.apache.camel.support.DefaultComponent;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("izghub")
public class IZGHubComponent extends DefaultComponent {
    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        return new IZGHubEndpoint(uri, this);
    }
}