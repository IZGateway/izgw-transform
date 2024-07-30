package gov.cdc.izgateway.transformation;

import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.enums.DataType;
import gov.cdc.izgateway.transformation.pipelines.DataPipeline;
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
public class TSApplicationController {

    private final PipelineBuilder pipelineBuilder;
    private final ServiceConfig serviceConfig;
    private final DataPipeline dataPipeline;

    @Autowired
    public TSApplicationController(ServiceConfig serviceConfig, PipelineBuilder pipelineBuilder, DataPipeline dataPipeline) {
        this.serviceConfig = serviceConfig;
        this.pipelineBuilder = pipelineBuilder;
        this.dataPipeline = dataPipeline;
    }

    @GetMapping("/hello")
    public String transform() {
        return "Hello from ApplicationController!";
    }

    @PutMapping("/transform")
    public String transform(@RequestBody String incomingMessage, @RequestHeader HttpHeaders headers) {
        try {

            // TODO - Org, IB, OB will likely come from camel.  This is for Demo only.
            UUID organization = UUID.fromString(headers.getFirst("X-izgw-organization"));
            String inboundEndpoint = headers.getFirst("X-izgw-ib");
            String outboundEndpoint = headers.getFirst("X-izgw-ob");

            ServiceContext context = new ServiceContext(organization,
                    inboundEndpoint,
                    outboundEndpoint,
                    DataType.HL7V2,
                    incomingMessage);

            // TODO - remove this when refactor is done
//            Hl7Pipeline pipeline = pipelineBuilder.build(context);
//            pipeline.execute(context);

            // NEW STUFF
            dataPipeline.execute(context);
            // END NEW STUFF

            // At this point request message has been transformed, we need to send it and deal with the response
            Message responseMessage = MllpSender.send("localhost", 21110, context.getRequestMessage());
            context.setCurrentDirection(DataFlowDirection.RESPONSE);
            context.setResponseMessage(responseMessage);

            // TODO - remove this when refactor is done
            //pipeline.execute(context);

            assert responseMessage != null;
            return responseMessage.encode();
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage());
            return e.getMessage();
        }
    }
}
