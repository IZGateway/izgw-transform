package gov.cdc.izgateway.transformation.context;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.enums.DataFlowDirection;
import gov.cdc.izgateway.transformation.enums.DataType;
import gov.cdc.izgateway.transformation.util.Hl7Utils;
import lombok.Data;

import java.util.HashMap;
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
    private HashMap<String, String> state;
    public String facilityId;

    public ServiceContext(UUID organizationId, String inboundEndpoint, String outboundEndpoint, DataType dataType, String facilityId, String rawMessage) throws HL7Exception {
        this.organizationId = organizationId;
        this.inboundEndpoint = inboundEndpoint;
        this.outboundEndpoint = outboundEndpoint;
        this.dataType = dataType;
        this.facilityId = facilityId;

        if (dataType.equals(DataType.HL7V2)) {
            this.requestMessage = Hl7Utils.parseHl7v2Message(rawMessage);
        }
        this.currentDirection = DataFlowDirection.REQUEST;

        this.state = new HashMap<>();
    }

    public Message getCurrentMessage() {
        // TODO - need to generalize "message" here so it can be HL7 or FHIR or whatever
        if (this.currentDirection == DataFlowDirection.REQUEST) {
            return this.requestMessage;
        }

        return this.responseMessage;
    }

    public void setCurrentMessage(Message message) {
        if (this.currentDirection == DataFlowDirection.REQUEST) {
            this.requestMessage = message;
        } else if (this.currentDirection == DataFlowDirection.RESPONSE) {
            this.responseMessage = message;
        }
    }

}
