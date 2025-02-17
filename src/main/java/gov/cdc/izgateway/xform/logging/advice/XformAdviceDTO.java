package gov.cdc.izgateway.xform.logging.advice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.UUID;

@Data
public class XformAdviceDTO {

    public XformAdviceDTO() {
    }

    public XformAdviceDTO(UUID id, String className, String name) {
        this.className = className;
        this.name = name;
        this.id = id;
    }

    private UUID id;
    private boolean processError;
    private String className;
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
