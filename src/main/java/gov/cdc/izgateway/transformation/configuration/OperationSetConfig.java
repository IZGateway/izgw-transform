package gov.cdc.izgateway.transformation.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class OperationSetConfig extends OperationConfig {
    private String destinationField;
    private String setValue;
}
