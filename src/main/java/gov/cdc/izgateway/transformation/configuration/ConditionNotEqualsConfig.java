package gov.cdc.izgateway.transformation.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class ConditionNotEqualsConfig extends OperationConfig {
    // TODO - ConditionNotEqualsConfig & ConditionEqualsConfig both have the same fields
    //        figure out a better way to do this. Interface OperationConfig so
    //        this could extend ConditionEqualsConfig but implemenets OperationConfig?
    private String fieldName;
    private String fieldValue;
}
