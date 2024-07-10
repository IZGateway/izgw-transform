package gov.cdc.izgateway.transformation.logging.advice;

import ca.uhn.hl7v2.model.Message;
import lombok.Data;

@Data
public class XformAdvice {
    String className;
    String methodName;
    String detail;
    String requestMessage;
    String responseMessage;

    public XformAdvice(String className, String methodName, String detail, String requestMessage, String responseMessage) {
        this.className = className;
        this.methodName = methodName;
        this.detail = detail;
        this.requestMessage = requestMessage;
        this.responseMessage = responseMessage;
    }

    public XformAdvice(String className, String methodName, String detail) {
        this.className = className;
        this.methodName = methodName;
        this.detail = detail;
    }
}
