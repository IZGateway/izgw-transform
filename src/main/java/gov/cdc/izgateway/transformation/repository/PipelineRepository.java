package gov.cdc.izgateway.transformation.repository;

import gov.cdc.izgateway.transformation.model.Pipeline;

import java.util.Set;
import java.util.UUID;

public interface PipelineRepository {
    public Pipeline getPipeline(UUID id);
    public Set<Pipeline> getPipelineSet();
}
