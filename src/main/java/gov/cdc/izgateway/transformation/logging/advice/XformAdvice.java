package gov.cdc.izgateway.transformation.logging.advice;

import lombok.Data;

@Data
public class XformAdvice {

    public XformAdvice(String className, String name) {
        this.className = className;
        this.name = name;
    }

    /*
     * name: "Zip Fixer"
     * request:
     * transformedRequest:
     * response:
     * transformedResponse:
     *
     * sdsd
     */

    private String className;
    private String name;
    private String request;
    private String transformedRequest;
    private String response;
    private String transformedResponse;
}
