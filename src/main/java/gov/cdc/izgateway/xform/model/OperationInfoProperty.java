package gov.cdc.izgateway.xform.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * Describes one configuration property of an operation type.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class OperationInfoProperty {
    private String type;
    private String description;
    private boolean required;

    /**
     * Creates an empty property description.
     */
    public OperationInfoProperty() {
        // The service sets the fields.
    }
}
