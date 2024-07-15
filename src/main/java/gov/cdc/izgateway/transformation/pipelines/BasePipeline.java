package gov.cdc.izgateway.transformation.pipelines;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.transformation.configuration.PipelineConfig;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.logging.advice.Advisable;

// TODO - Need to make generic, meaning not using HAPI at this level because
//        classes that extend this will not all be HL7 related

abstract class BasePipeline implements Pipeline, Advisable {

    PipelineConfig configuration;

    protected BasePipeline() {

    }

    protected BasePipeline(PipelineConfig pipelineConfig) {
        this.configuration = pipelineConfig;

    }

    @Override
    public void execute(ServiceContext context) throws HL7Exception {
        if (configuration != null) {
            executeThisPipeline(context);
        }
    }

    @Override
    public String getName() {
        return configuration.getName();
    }

    @Override
    public String getId() {
        return configuration.getId().toString();
    }

    @Override
    public boolean hasTransformed() {
        return true;
    }

    @Override
    public boolean preconditionPassed() {
        return true;
    }

    public abstract void executeThisPipeline(ServiceContext context) throws HL7Exception;
}
