package gov.cdc.izgateway.transformation.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
@Setter
public class PipelineConfig {
    private String pipelineName;
    private String inboundEndpoint;
    private String outboundEndpoint;
    private List<DataTransformationConfig> requestTransformations;
    private List<DataTransformationConfig> responseTransformations;
}
