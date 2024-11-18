package gov.cdc.izgateway.xform.operations;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.cdc.izgateway.xform.context.ServiceContext;
import gov.cdc.izgateway.xform.exceptions.OperationException;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "method")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Copy.class, name = "copy"),
        @JsonSubTypes.Type(value = Mapper.class, name = "mapper"),
        @JsonSubTypes.Type(value = RegexReplace.class, name = "regex_replace"),
        @JsonSubTypes.Type(value = Set.class, name = "set"),
        @JsonSubTypes.Type(value = SaveState.class, name = "save_state")
})
public interface Operation {
    void execute(ServiceContext context) throws OperationException;
}
