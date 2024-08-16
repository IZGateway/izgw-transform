package gov.cdc.izgateway.transformation.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class OperationMapperConfig extends OperationConfig {
    private String codeField;
    private String codeSystemField;
    private String codeSystemDefault;
}
