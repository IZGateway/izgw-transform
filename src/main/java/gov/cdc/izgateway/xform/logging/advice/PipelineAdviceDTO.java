package gov.cdc.izgateway.xform.logging.advice;

import lombok.Data;

import java.util.UUID;

@Data
public class PipelineAdviceDTO extends XformAdviceDTO {
    public PipelineAdviceDTO() {
    }

    public PipelineAdviceDTO(UUID id, String className, String name) {
        super(id, className, name);
    }
}
