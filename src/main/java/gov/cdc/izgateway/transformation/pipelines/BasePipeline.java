package gov.cdc.izgateway.transformation.pipelines;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import gov.cdc.izgateway.transformation.configuration.PipelineConfig;

abstract class BasePipeline implements Pipeline {

    Pipeline nextPipeline;
    PipelineConfig configuration;

    protected BasePipeline(PipelineConfig pipelineConfig) {
        this.configuration = pipelineConfig;

    }

    @Override
    public void execute(Message message, String direction) throws HL7Exception {
        executeThisPipeline(message, direction);
        executeNextPipeline(message, direction);
    }

    @Override
    public void setNextPipeline(Pipeline nextPipeline) {
        this.nextPipeline = nextPipeline;
    }

    @Override
    public Pipeline getNextPipeline() {
        return nextPipeline;
    }

    public abstract void executeThisPipeline(Message message, String direction) throws HL7Exception;

    public void executeNextPipeline(Message message, String direction) throws HL7Exception {
        if (nextPipeline != null) {
            nextPipeline.execute(message, direction);
        }
    }
}
