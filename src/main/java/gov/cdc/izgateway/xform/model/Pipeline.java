package gov.cdc.izgateway.xform.model;

import gov.cdc.izgateway.xform.validation.ValidOrganization;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Pipeline implements BaseModel {

    @NotBlank(message = "Pipeline name is required")
    private String pipelineName;

    @NotNull(message = "Pipeline ID is required")
    private UUID id;

    @NotNull(message = "Organization ID is required")
    @ValidOrganization(message = "Organization ID must reference an existing and active organization")
    private UUID organizationId;

    private String description;

    @NotBlank(message = "Inbound endpoint is required")
    private String inboundEndpoint;

    @NotBlank(message = "Outbound endpoint is required")
    private String outboundEndpoint;

    @NotNull(message = "Pipeline active status is required")
    private Boolean active;

    @NotNull(message = "Pipes List is required (can be empty)")
    @Valid
    private List<Pipe> pipes;
}
