package gov.cdc.izgateway.transformation.configuration;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "method")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OperationCopyConfig.class, name = "copy"),
        @JsonSubTypes.Type(value = OperationSetConfig.class, name = "set"),
        @JsonSubTypes.Type(value = ConditionEqualsConfig.class, name = "equals"),
        @JsonSubTypes.Type(value = ConditionNotEqualsConfig.class, name = "not_equals"),
        @JsonSubTypes.Type(value = OperationRegexReplaceConfig.class, name = "regex_replace"),
        @JsonSubTypes.Type(value = ConditionEmptyConfig.class, name = "empty"),
        @JsonSubTypes.Type(value = ConditionNotEmptyConfig.class, name = "not_empty"),
        @JsonSubTypes.Type(value = OperationMapperConfig.class, name = "mapper")
})
public class OperationConfig {
    private int order;
}
