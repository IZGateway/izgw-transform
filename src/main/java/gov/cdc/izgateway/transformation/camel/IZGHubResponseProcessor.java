package gov.cdc.izgateway.transformation.camel;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class IZGHubResponseProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        ServiceContext context = exchange.getIn().getBody(ServiceContext.class);
        Message message = context.getRequestMessage();
        //Message message = exchange.getIn().getBody(Message.class);

        Terser terser = new Terser(message);

        terser.set("/MSH-13-1", "NEW RESPONSE");

        // exchange.getIn().setBody(message);
        // context.setHl7Message(message);
    }
}
