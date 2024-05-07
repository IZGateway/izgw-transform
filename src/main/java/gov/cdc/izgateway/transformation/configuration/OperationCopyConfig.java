package gov.cdc.izgateway.transformation.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class OperationCopyConfig extends OperationConfig {
    private String sourceField;
    private String destinationField;
}
