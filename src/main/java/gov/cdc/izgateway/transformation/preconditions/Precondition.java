package gov.cdc.izgateway.transformation.preconditions;

import ca.uhn.hl7v2.model.Message;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "method")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Equals.class, name = "equals"),
        @JsonSubTypes.Type(value = NotEquals.class, name = "not_equals"),
        @JsonSubTypes.Type(value = Exists.class, name = "exists"),
        @JsonSubTypes.Type(value = NotExists.class, name = "not_exists")
})
public interface Precondition {
    boolean evaluate(Message message);
}
