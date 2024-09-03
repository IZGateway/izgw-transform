package gov.cdc.izgateway.transformation.logging.advice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.UUID;

@Data
public class XformAdviceDTO {

    public XformAdviceDTO() {
    }

    public XformAdviceDTO(String className, String name) {
        this.className = className;
        this.name = name;
    }

    private boolean processError;
    private String className;
    private String destinationId;
    private UUID organizationId;
    private String name;
    @JsonIgnore
    private String request;
    @JsonIgnore
    private String transformedRequest;
    @JsonIgnore
    private String response;
    @JsonIgnore
    private String transformedResponse;

}
