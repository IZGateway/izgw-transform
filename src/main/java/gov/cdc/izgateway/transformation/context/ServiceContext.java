package gov.cdc.izgateway.transformation.context;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import lombok.Data;

import java.io.IOException;

@Data
public class ServiceContext {
    private String organizationName;
    private String inboundEndpoint;
    private String outboundEndpoint;
    private Message requestMessage;
    private Message responseMessage;

    public ServiceContext(String organizationName, String inboundEndpoint, String outboundEndpoint, String rawHl7Message) throws HL7Exception {
        this.organizationName = organizationName;
        this.inboundEndpoint = inboundEndpoint;
        this.outboundEndpoint = outboundEndpoint;

        this.requestMessage = parseMessage(rawHl7Message);
    }

    private Message parseMessage(String rawHl7Message) throws HL7Exception {
        PipeParser parser;
        try (DefaultHapiContext context = new DefaultHapiContext()) {
            context.setValidationContext(new NoValidation());
            parser = context.getPipeParser();
        } catch (IOException e) {
            throw new HL7Exception(e);
        }

        return parser.parse(rawHl7Message);
    }
}
