package gov.cdc.izgateway.xform.logging.advice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.UUID;

/**
 * Base class for the advice that the advice aspect records about an advised object.
 */
@Data
public class XformAdviceDTO {

    /**
     * Creates empty advice.
     */
    public XformAdviceDTO() {
    }

    /**
     * Creates advice for the identified advised object.
     *
     * @param id the identifier of the advised object
     * @param className the simple class name of the advised object
     * @param name the name of the advised object
     */
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
