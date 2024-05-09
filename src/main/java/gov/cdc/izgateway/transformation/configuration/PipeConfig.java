package gov.cdc.izgateway.transformation.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
@Setter
public class PipeConfig {
    private List<OperationConfig> preconditions;
    // TODO - solutions may end up w/ unique id's instead of names?
    private String solution;
    private String version;
}
