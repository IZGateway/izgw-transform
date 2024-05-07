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
        @JsonSubTypes.Type(value = OperationEqualsConfig.class, name = "equals")
})
public class OperationConfig {
    private int order;
}
