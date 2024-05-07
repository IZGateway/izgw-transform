package gov.cdc.izgateway.transformation.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
@Setter
public class DataTransformationConfig {
    private OperationConfig precondition;
    private List<OperationConfig> operationList;
}
