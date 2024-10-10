package gov.cdc.izgateway.transformation.logging.advice;

import lombok.Data;

import java.util.UUID;

@Data
public class PipelineAdviceDTO extends XformAdviceDTO {
    private UUID id;


    public PipelineAdviceDTO() {
    }

    public PipelineAdviceDTO(UUID id, String className, String name) {
        super(className, name);
        this.id = id;
    }

}
