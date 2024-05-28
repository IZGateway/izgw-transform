package gov.cdc.izgateway.transformation.context;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.enums.DataType;
import gov.cdc.izgateway.transformation.configuration.ServiceConfig;
import lombok.Data;

import java.io.IOException;
import java.util.UUID;

@Data
public class ServiceContext {
    private UUID organizationId;
    private String inboundEndpoint;
    private String outboundEndpoint;
    private Message requestMessage;
    private Message responseMessage;
    private DataFlowDirection currentDirection;
    private DataType dataType;

    public ServiceContext(UUID organizationId, String inboundEndpoint, String outboundEndpoint, DataType dataType, String rawMessage) throws HL7Exception {
        this.organizationId = organizationId;
        this.inboundEndpoint = inboundEndpoint;
        this.outboundEndpoint = outboundEndpoint;
        this.dataType = dataType;

        if (dataType.equals(DataType.HL7V2)) {
            this.requestMessage = parseHl7v2Message(rawMessage);
        }
        this.currentDirection = DataFlowDirection.REQUEST;
    }

    private Message parseHl7v2Message(String rawHl7Message) throws HL7Exception {
        PipeParser parser;
        try (DefaultHapiContext context = new DefaultHapiContext()) {
            // This replacement just here because my SOAP client is messing w/ EOL characters
            rawHl7Message = rawHl7Message.replace("\n", "\r");
            context.setValidationContext(new NoValidation());
            parser = context.getPipeParser();
        } catch (IOException e) {
            throw new HL7Exception(e);
        }

        return parser.parse(rawHl7Message);
    }
}
