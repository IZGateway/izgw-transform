package gov.cdc.izgateway.transformation.preconditions;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.cdc.izgateway.transformation.context.ServiceContext;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "method")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Equals.class, name = "equals"),
        @JsonSubTypes.Type(value = NotEquals.class, name = "not_equals"),
        @JsonSubTypes.Type(value = Exists.class, name = "exists"),
        @JsonSubTypes.Type(value = NotExists.class, name = "not_exists"),
        @JsonSubTypes.Type(value = RegexMatch.class, name = "regex_match")
})
public interface Precondition {
    boolean evaluate(ServiceContext context);
}
