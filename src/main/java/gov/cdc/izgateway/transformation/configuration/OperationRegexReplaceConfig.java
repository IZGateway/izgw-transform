package gov.cdc.izgateway.transformation.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class OperationRegexReplaceConfig extends OperationConfig {
    private String field;
    private String regex;
}
