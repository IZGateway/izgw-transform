package gov.cdc.izgateway.transformation.logging.advice;

import lombok.Data;

@Data
public class PipelineAdviceDTO extends XformAdviceDTO {
    private String id;


    public PipelineAdviceDTO() {
    }

    public PipelineAdviceDTO(String id, String className, String name) {
        super(className, name);
        this.id = id;
    }

}
