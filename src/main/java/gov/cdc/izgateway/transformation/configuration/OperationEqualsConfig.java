package gov.cdc.izgateway.transformation.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class OperationEqualsConfig extends OperationConfig {
    private String fieldName;
    private String fieldValue;
}
