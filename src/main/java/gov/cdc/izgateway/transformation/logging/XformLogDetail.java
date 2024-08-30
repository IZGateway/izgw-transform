package gov.cdc.izgateway.transformation.logging;

import lombok.Data;

@Data
public class XformLogDetail {
    private String eventId;
    private String concept;
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
}
