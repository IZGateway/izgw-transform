package gov.cdc.izgateway.transformation.pipelines;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.transformation.configuration.PipelineConfig;
import gov.cdc.izgateway.transformation.context.ServiceContext;

// TODO - Need to make generic, meaning not using HAPI at this level because
//        classes that extend this will not all be HL7 related

abstract class BasePipeline implements Pipeline {

    Pipeline nextPipeline;
    PipelineConfig configuration;

    protected BasePipeline(PipelineConfig pipelineConfig) {
        this.configuration = pipelineConfig;

    }

    @Override
    public void execute(ServiceContext context) throws HL7Exception {
        executeThisPipeline(context);
        executeNextPipeline(context);
    }

    @Override
    public void setNextPipeline(Pipeline nextPipeline) {
        this.nextPipeline = nextPipeline;
    }

    @Override
    public Pipeline getNextPipeline() {
        return nextPipeline;
    }

    public abstract void executeThisPipeline(ServiceContext context) throws HL7Exception;

    public void executeNextPipeline(ServiceContext context) throws HL7Exception {
        if (nextPipeline != null) {
            nextPipeline.execute(context);
        }
    }
}
