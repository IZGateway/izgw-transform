package gov.cdc.izgateway.transformation;

import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.chains.PipelineChain;
import gov.cdc.izgateway.transformation.chains.PipelineChainBuilder;
import gov.cdc.izgateway.transformation.configuration.ServiceConfig;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.mllp.MllpSender;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.logging.Level;

@Log
@RestController
public class ApplicationController {

    @Autowired
    ServiceConfig serviceConfig;

    @Autowired
    PipelineChainBuilder pipelineBuilder;

    @GetMapping("/hello")
    public String transform() {
        return "Hello from ApplicationController!";
    }

    @PutMapping("/transform")
    public String transform(@RequestBody String incomingMessage, @RequestHeader HttpHeaders headers) {
        try {

            // TODO - need to figure out how to determine Org, IB & OB
            // TODO - this is all just for DEMO
            UUID organization = UUID.fromString(headers.getFirst("X-izgw-organization"));
            String inboundEndpoint = headers.getFirst("X-izgw-ib");
            String outboundEndpoint = headers.getFirst("X-izgw-ob");

            ServiceContext context = new ServiceContext(organization, inboundEndpoint, outboundEndpoint, incomingMessage);

            PipelineChain pipeline = pipelineBuilder.build(context);
            pipeline.execute(context.getRequestMessage(), "request");

            // At this point request message has been transformed, we need to send it and deal with the response
            Message responseMessage = MllpSender.send("localhost", 6661, context.getRequestMessage());

            pipeline.execute(responseMessage, "response");

            assert responseMessage != null;
            return responseMessage.encode();
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage());
            return e.getMessage();
        }
    }
}
