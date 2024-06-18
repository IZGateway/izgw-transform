package gov.cdc.izgateway.transformation.context;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.enums.DataType;
import gov.cdc.izgateway.transformation.util.Hl7Utils;
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
            this.requestMessage = Hl7Utils.parseHl7v2Message(rawMessage);
        }
        this.currentDirection = DataFlowDirection.REQUEST;
    }

}
