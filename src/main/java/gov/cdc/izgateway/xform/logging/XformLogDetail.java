package gov.cdc.izgateway.xform.logging;

import lombok.Data;

import java.util.UUID;

/**
 * Details of a transformed transaction for one pipeline that {@link XformTransactionData} writes to the log.
 */
@Data
public class XformLogDetail {
    private String eventId;
    private String concept;
    private String destinationId;
    private UUID organizationId;
    private boolean processError;
    private String name;
    private String requestMessageType;
    private String requestSendingApplication;
    private String requestSendingFacility;
    private String requestReceivingApplication;
    private String requestReceivingFacility;
    private String responseSendingApplication;
    private String responseSendingFacility;
    private String responseReceivingApplication;
    private String responseReceivingFacility;

    /**
     * Creates an empty log detail.
     */
    public XformLogDetail() {
        // Lombok generates the accessors.
    }
}
