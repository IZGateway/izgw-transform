package gov.cdc.izgateway.xform.context;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.xform.enums.DataFlowDirection;
import gov.cdc.izgateway.xform.enums.DataType;
import gov.cdc.izgateway.xform.util.Hl7Utils;
import lombok.Data;

import java.util.HashMap;
import java.util.UUID;

/**
 * Mutable per-message state that is passed through a pipeline, including the request and response messages and the current data flow direction.
 */
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
    /** The facility identifier of the message, compared by the facility preconditions. */
    public String facilityId;

    /**
     * Creates a context in the request direction and parses the raw message if the data type is HL7 v2.
     *
     * @param organizationId the organization identifier
     * @param inboundEndpoint the inbound endpoint
     * @param outboundEndpoint the outbound endpoint
     * @param dataType the data type of the message
     * @param facilityId the facility identifier
     * @param rawMessage the raw message text
     * @throws HL7Exception if the HL7 v2 message cannot be parsed
     */
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

    /**
     * Gets the message for the current direction.
     *
     * @return the request message in the request direction, otherwise the response message
     */
    public Message getCurrentMessage() {
        // TODO - need to generalize "message" here so it can be HL7 or FHIR or whatever
        if (this.currentDirection == DataFlowDirection.REQUEST) {
            return this.requestMessage;
        }

        return this.responseMessage;
    }

    /**
     * Sets the message for the current direction.
     *
     * @param message the message to set as the request or response message
     */
    public void setCurrentMessage(Message message) {
        if (this.currentDirection == DataFlowDirection.REQUEST) {
            this.requestMessage = message;
        } else if (this.currentDirection == DataFlowDirection.RESPONSE) {
            this.responseMessage = message;
        }
    }

}
