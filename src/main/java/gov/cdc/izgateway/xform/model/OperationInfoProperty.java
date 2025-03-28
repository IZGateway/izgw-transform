package gov.cdc.izgateway.xform.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class OperationInfoProperty {
    private String type;
    private String description;
    private boolean required;
}
