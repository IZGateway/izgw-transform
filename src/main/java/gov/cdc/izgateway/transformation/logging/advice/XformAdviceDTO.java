package gov.cdc.izgateway.transformation.logging.advice;

import lombok.Data;

@Data
public class XformAdviceDTO {

    public XformAdviceDTO() {
    }

    public XformAdviceDTO(String className, String name) {
        this.className = className;
        this.name = name;
    }

    private String className;
    private String name;
    private String request;
    private String transformedRequest;
    private String response;
    private String transformedResponse;

}
