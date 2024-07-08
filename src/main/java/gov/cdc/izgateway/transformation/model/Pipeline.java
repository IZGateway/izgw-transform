package gov.cdc.izgateway.transformation.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Pipeline {
    private String name;
    private UUID id;
    private UUID organizationId;
    private String description;
    private String inboundEndpoint;
    private String outboundEndpoint;
    private Boolean active;
}
