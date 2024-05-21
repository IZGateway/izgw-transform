package gov.cdc.izgateway.transformation.context;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.enums.DataType;
import gov.cdc.izgateway.transformation.configuration.OrganizationConfig;
import gov.cdc.izgateway.transformation.configuration.PipelineConfig;
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
    // TODO - remove configuration from context, will be pulled in other classes as needed
    private ServiceConfig configuration;
    private DataFlowDirection currentDirection;
    private DataType dataType;

    public ServiceContext(UUID organizationId, String inboundEndpoint, String outboundEndpoint, ServiceConfig configuration, DataType dataType, String rawMessage) throws HL7Exception {
        this.organizationId = organizationId;
        this.inboundEndpoint = inboundEndpoint;
        this.outboundEndpoint = outboundEndpoint;
        this.configuration = configuration;
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

    public OrganizationConfig getOrganization() {
        return configuration
                .getOrganizations()
                .stream()
                .filter(org -> org.getOrganizationId().equals(organizationId))
                .reduce((a, b) -> {
                    throw new IllegalStateException("More than one OrganizationConfig found for id " + organizationId);
                }).orElse(null);
    }

    public PipelineConfig getPipeline() {
        return getOrganization().getPipelines()
                .stream()
                .filter(pl -> pl.getInboundEndpoint().equals(inboundEndpoint) && pl.getOutboundEndpoint().equals(outboundEndpoint))
                .reduce((a, b) -> {
                    throw new IllegalStateException(String.format("More than one PipelineConfig found for Organization ID '%s', Inbound Endpoint '%s', and Outbound Endpoint '%s'",
                            outboundEndpoint,
                            inboundEndpoint,
                            outboundEndpoint));
                }).orElse(null);
    }
}
