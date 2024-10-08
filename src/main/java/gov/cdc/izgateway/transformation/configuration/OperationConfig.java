package gov.cdc.izgateway.transformation.configuration;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "method")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OperationCopyConfig.class, name = "copy"),
        @JsonSubTypes.Type(value = OperationSetConfig.class, name = "set"),
        @JsonSubTypes.Type(value = OperationRegexReplaceConfig.class, name = "regex_replace"),
        @JsonSubTypes.Type(value = OperationSaveStateConfig.class, name = "save_state"),
        @JsonSubTypes.Type(value = OperationMapperConfig.class, name = "mapper")
})
public class OperationConfig {
    private int order;
    @NotNull(message = "Organization ID is required")
    private UUID id;
}
