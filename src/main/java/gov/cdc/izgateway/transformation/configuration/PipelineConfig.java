package gov.cdc.izgateway.transformation.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Getter
@Setter
public class PipelineConfig {
    private String name;
    private UUID id;
    private String description;
    private String inboundEndpoint;
    private String outboundEndpoint;
    private List<PipeConfig> pipes;
}
