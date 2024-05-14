package gov.cdc.izgateway.transformation.pipelines;

import ca.uhn.hl7v2.HL7Exception;
import gov.cdc.izgateway.transformation.configuration.PipelineConfig;
import gov.cdc.izgateway.transformation.context.ServiceContext;
import gov.cdc.izgateway.transformation.pipes.Hl7v2Pipe;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Log
public class Hl7Pipeline extends BasePipeline implements Pipeline {

    List<Hl7v2Pipe> pipes;


    public Hl7Pipeline(PipelineConfig pipelineConfig) {
        super(pipelineConfig);
        pipes = new ArrayList<>();
    }

    @Override
    public void executeThisPipeline(ServiceContext context) throws HL7Exception {
        log.log(Level.WARNING, String.format("Executing %s Pipeline: %s", context.getCurrentDirection(), this.configuration.getName()));

        for (Hl7v2Pipe pipe : pipes) {
            pipe.execute(context);
        }

    }

    public void addPipe(Hl7v2Pipe pipe) {
        pipes.add(pipe);
    }
}
