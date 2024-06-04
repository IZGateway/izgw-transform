package gov.cdc.izgateway.transformation.camel;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

public class IZGHubProducer  extends DefaultProducer {
    public IZGHubProducer(Endpoint endpoint) {
        super(endpoint);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        System.out.println("IZGHubProducer");
        System.out.println("Endpoint: " + getEndpoint().getEndpointUri());
        // Your custom logic here
        String body = exchange.getIn().getBody(String.class);
        WebClient webClient = WebClient.create();
        String response = webClient.put()
                .uri("http://localhost:8081/izghubstub")
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("Response: " + response);

    }

}
