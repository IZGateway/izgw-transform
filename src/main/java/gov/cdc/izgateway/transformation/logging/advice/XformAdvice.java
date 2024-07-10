package gov.cdc.izgateway.transformation.logging.advice;

import ca.uhn.hl7v2.model.Message;
import lombok.Data;

@Data
public class XformAdvice {
    Action action;
    String detail;
    String requestMessage;
    String responseMessage;

    public XformAdvice(Action action, String detail, String requestMessage, String responseMessage) {
        this.action = action;
        this.detail = detail;
        this.requestMessage = requestMessage;
        this.responseMessage = responseMessage;
    }

    public XformAdvice(Action action, String detail) {
        this.action = action;
        this.detail = detail;
    }
}
