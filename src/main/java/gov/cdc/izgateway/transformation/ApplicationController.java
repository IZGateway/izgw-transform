package gov.cdc.izgateway.transformation;

import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.pipelines.PipelineBuilder;
import gov.cdc.izgateway.transformation.configuration.ServiceConfig;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.mllp.MllpSender;
import gov.cdc.izgateway.transformation.pipelines.Hl7Pipeline;
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
    PipelineBuilder pipelineBuilder;

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

            // TODO - passing config to context, mainly because when we get to the point of
            //        building the pipes we need to be able to lookup details of the Solutions
            //        from the solutions section in the configuration.  But that was not there.
            //        Has to be a better way to do this Spring-y Boot-y.  Going old school
            //        for now.
            ServiceContext context = new ServiceContext(organization,
                    inboundEndpoint,
                    outboundEndpoint,
                    serviceConfig,
                    incomingMessage);

            Hl7Pipeline pipeline = pipelineBuilder.build(context);
            pipeline.execute(context);

            // At this point request message has been transformed, we need to send it and deal with the response
            Message responseMessage = MllpSender.send("localhost", 21110, context.getRequestMessage());
            context.setCurrentDirection("RESPONSE");
            context.setResponseMessage(responseMessage);

            pipeline.execute(context);

            assert responseMessage != null;
            return responseMessage.encode();
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage());
            return e.getMessage();
        }
    }
}
